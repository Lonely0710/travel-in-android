package com.bjtu.traveler.utils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DeepSeekApiClient {

    private static final String TAG = "DeepSeekApiClient";
    private static String API_KEY = null;
    private static final String API_URL = "https://api.deepseek.com/chat/completions";
    private static final String MODEL_NAME = "deepseek-chat";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS) // AI响应可能较长
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final Gson gson = new Gson();

    public static void init(String apiKey) {
        API_KEY = apiKey;
    }

    /**
     * 发送聊天请求到 DeepSeek API。
     *
     * @param systemPrompt 系统提示，定义AI的角色和行为。
     * @param userMessage 用户输入。
     * @return AI的响应内容，或 null 如果发生错误。
     */
    private static String sendChatRequest(String systemPrompt, String userMessage) throws IOException {
        if (API_KEY == null || API_KEY.isEmpty()) {
            Log.e(TAG, "DeepSeek API Key not initialized.");
            throw new IOException("DeepSeek API Key not initialized.");
        }

        JsonObject messageSystem = new JsonObject();
        messageSystem.addProperty("role", "system");
        messageSystem.addProperty("content", systemPrompt);

        JsonObject messageUser = new JsonObject();
        messageUser.addProperty("role", "user");
        messageUser.addProperty("content", userMessage);

        JsonArray messages = new JsonArray();
        messages.add(messageSystem);
        messages.add(messageUser);

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("model", MODEL_NAME);
        jsonBody.add("messages", messages);
        jsonBody.addProperty("stream", false); // 不使用流式传输

        RequestBody body = RequestBody.create(gson.toJson(jsonBody), JSON_MEDIA_TYPE);

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        Log.d(TAG, "Sending DeepSeek API request. Payload: " + gson.toJson(jsonBody));

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            Log.d(TAG, "DeepSeek API response: " + response.code() + ", Body: " + responseBody);

            if (!response.isSuccessful()) {
                throw new IOException("DeepSeek API request failed: " + response.code() + " - " + responseBody);
            }

            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            if (choices != null && choices.size() > 0) {
                JsonObject firstChoice = choices.get(0).getAsJsonObject();
                JsonObject message = firstChoice.getAsJsonObject("message");
                if (message != null && message.has("content")) {
                    return message.get("content").getAsString();
                }
            }
            return null; // 未能从响应中获取内容
        }
    }

    /**
     * 简化用户输入，提取核心信息。
     *
     * @param rawInput 用户原始输入。
     * @param contextPurpose 期望提取的信息类型（例如："旅游目的地", "天数", "预算", "偏好"）。
     * @return 简化后的字符串。
     */
    public static String getSimplifiedInput(String rawInput, String contextPurpose) throws IOException {
        String systemPrompt = String.format(
                "你是一个信息提取助手，只负责从用户输入中提取与%s相关最简洁、核心的信息。用户的输入只有可能是以下几种情况：1. 旅游目的地；2. 旅游天数；3. 旅游预算；4. 期望的旅游风格。而你只需要判断用户的输入是哪种情况，并据此回复且仅回复用户输入中的核心信息。例如针对情况1，则回复“北京”“杭州”这类的地名；针对情况2，则回复“2”“5”这类的纯数字的天数；针对情况3，则回复“3000”“4500”这类的纯数字的金额；针对情况4，则回复”美食+文化+悠闲+雅致“这种由4个简短词语组成、'+'分开的字符串。如果用户输入的内容无法提取出有效信息，请直接回复'null'，不要回复其他任何内容。不要进行任何解释或对话。",
                contextPurpose
        );
        return sendChatRequest(systemPrompt, rawInput);
    }

    /**
     * 根据收集到的信息和爬取的景点数据生成旅游规划。
     *
     * @param travelPlan 包含目的地、天数、预算、偏好等信息的 TravelPlan 对象。
     * @param attractionsJson 爬取到的景点信息的JSON字符串（通常是PlaceScraper.Destination列表的JSON）。
     * @return AI生成的JSON格式旅游规划。
     */
    public static String getTravelPlan(
            com.bjtu.traveler.data.model.TravelPlan travelPlan,
            String attractionsJson) throws IOException {

        String systemPrompt = "你是一个专业的旅游规划师，根据用户提供的需求和给定城市的景点数据，生成一个详细的旅游行程。请以JSON格式输出，不要包含任何额外的文本或解释。JSON结构应该是一个数组，每个元素代表一天的行程，对应于 'DayPlan' 结构。每一天的行程包含 'dayTitle'（格式如 'Day X: 主题'）, 以及 'morningActivity', 'afternoonActivity', 'eveningActivity'。这三个 'Activity' 字段的值应该是一个 JSON 字符串，这个字符串本身是一个包含完整景点信息的 JSON 对象，包含 'name', 'description', 'address', 'price', 'detailPageUrl', 'businessId', 'commentScore', 'sightLevel', 'coverImageUrl', 'tagNames', 'latitude', 'longitude', 'heatScore' 这些键。如果某个时段没有合适的景点，该 'Activity' 字段的值可以是一个空对象的 JSON 字符串 '{}' 或 'null'。\n" +
                "JSON格式示例：\n" +
                "[\n" +
                "  {\n" +
                "    \"dayTitle\": \"Day 1: 历史文化探索\",\n" + // AI 生成的 DayPlan.dayTitle
                "    \"morningActivity\": \"{\\\"name\\\":\\\"故宫博物院\\\",\\\"description\\\":\\\"中国最大的皇家宫殿...\\\",\\\"address\\\":\\\"北京市东城区...\\\",\\\"price\\\":60,\\\"detailPageUrl\\\":\\\"...\\\",\\\"businessId\\\":\\\"229\\\",\\\"commentScore\\\":4.8,\\\"sightLevel\\\":\\\"5A\\\",\\\"coverImageUrl\\\":\\\"...\\\",\\\"tagNames\\\":[\\\"展馆展览\\\",\\\"遛娃宝藏地\\\"],\\\"latitude\\\":39.924091,\\\"longitude\\\":116.403414,\\\"heatScore\\\":10.0}\",\n" + // 这是一个JSON字符串
                "    \"morningIconType\": \"历史\",\n" + // AI 根据景点类型生成一个关键词
                "    \"afternoonActivity\": \"{\\\"name\\\":\\\"天安门广场\\\", ... }\",\n" +
                "    \"afternoonIconType\": \"广场\",\n" +
                "    \"eveningActivity\": \"{\\\"name\\\":\\\"王府井大街\\\", ... }\",\n" +
                "    \"eveningIconType\": \"商业\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"dayTitle\": \"Day 2: 自然风光\",\n" +
                "    \"morningActivity\": \"{\\\"name\\\":\\\"八达岭长城\\\", ... }\",\n" +
                "    \"morningIconType\": \"长城\",\n" +
                "    \"afternoonActivity\": \"{}\",\n" + // 或 null
                "    \"afternoonIconType\": null,\n" +
                "    \"eveningActivity\": \"{}\",\n" + // 或 null
                "    \"eveningIconType\": null\n" +
                "  }\n" +
                "]\n\n" +
                "请确保生成的景点名称与提供的attractionsJson列表中的景点名称完全匹配，并且每个景点的所有详细信息都直接从attractionsJson中原样获取并打包到对应的 'Activity' JSON 字符串中，不要自己编造或修改。如果某个景点在attractionsJson中没有提供某个字段，请在生成的JSON字符串中将其设置为null或空字符串或0（对于数字类型）。如果某个时段（上午/下午/晚上）没有合适的景点，将对应的 'Activity' 字段设置为一个空JSON对象字符串 '{}' 或字符串 'null'，并将对应的 'IconType' 字段设置为 null。严格按照此JSON格式输出。";

        String userMessage = String.format(
                "用户希望规划一个旅行：\n" +
                        "目的地：%s\n" +
                        "天数：%d 天\n" +
                        "预算：%d 元\n" +
                        "偏好：%s\n" +
                        "以下是该城市可用的景点数据（JSON格式）：\n%s",
                travelPlan.getDestination(), travelPlan.getDays(), travelPlan.getBudget(), travelPlan.getPreferences() != null ? String.join(", ", travelPlan.getPreferences()) : "无", attractionsJson
        );
        return sendChatRequest(systemPrompt, userMessage);
    }
}