package com.bjtu.traveler.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors; // 用于 String.join，如果API Level太低可以手动拼接

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Headers;
import okio.Buffer; // 确保已导入

public class PlaceScraper {

    private static final String TAG = "PlaceScraper";
    private static final String CITIES_CSV_FILENAME = "cities.csv";
    private static Map<String, String> cityPinyinToIdMap;

    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS) // 使用Python的timeout设置
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build();
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    // 从 Python settings.py 中复制过来的 Cookies 字符串
    // 注意：这将是一个非常长的字符串，包含所有 key=value 对，用分号和空格分隔
    private static final String PYTHON_COOKIES_STRING =
            "UBT_VID=1731476947273.a7b45HPTkTVR; GUID=09031076110042895602; MKT_CKID=1731476948365.c1kab.34ag; _RSG=GTagMfp4rWEXAJL7ZC63HA; _RDG=288775a6efa26f2d75343ff87fdeb2fc86; _RGUID=e835133a-56aa-4750-a443-18c8c75532a1; StartCity_Pkg=PkgStartCity=6; nfes_isSupportWebP=1; _bfaStatusPVSend=1; _RF1=123.177.53.139; _ubtstatus=%7B%22vid%22%3A%221731476947273.a7b45HPTkTVR%22%2C%22sid%22%3A18%2C%22pvid%22%3A10%2C%22pid%22%3A600001375%7D; _bfaStatus=success; ibulanguage=CN; ibulocale=zh_cn; cookiePricesDisplayed=CNY; _abtest_userid=f2d1418a-1dd3-4b74-a2ab-40cbb53019e2; appFloatCnt=1; Hm_lvt_a8d6737197d542432f4ff4abc6e06384=1731476947,1731835830,1731920792,1732847172; _ga=GA1.1.851142854.1731476948; Session=smartlinkcode=U130727&smartlinklanguage=zh&SmartLinkKeyWord=&SmartLinkQuary=&SmartLinkHost=; Union=AllianceID=4902&SID=130727&OUID=&createtime=1732847175&Expires=1733451974870; MKT_Pagesource=PC; _ga_9BZF483VNQ=GS1.1.1732860047.4.1.1732860071.0.0.0; _ga_5DVRDQD429=GS1.1.1732860047.4.1.1732860071.0.0.0; _ga_B77BES1Z8Z=GS1.1.1732860047.4.1.1732860071.36.0.0; _jzqco=%7C%7C%7C%7C1732875288179%7C1.1595707400.1732875276978.1732875276978.1732875342323.1732875276978.1732875342323.0.0.0.2.2; _bfa=1.1731476947273.a7b45HPTkTVR.1.1732875293312.1732875345206.1.2.10650142842;";

    /**
     * 初始化城市数据，从assets读取cities.csv。
     * 确保在调用scrapeCityDestinations之前调用此方法，且只调用一次。
     *
     * @param context 应用上下文
     */
    public static synchronized void initCityData(Context context) {
        if (cityPinyinToIdMap == null) {
            cityPinyinToIdMap = new HashMap<>();
            try {
                InputStream is = context.getAssets().open(CITIES_CSV_FILENAME);
                Log.d(TAG, "成功打开 " + CITIES_CSV_FILENAME + " 文件.");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                boolean isFirstLine = true; // 跳过CSV头部
                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }
                    String[] parts = line.split(","); // 假设CSV是逗号分隔
                    if (parts.length == 2) {
                        cityPinyinToIdMap.put(parts[0].trim(), parts[1].trim());
                    }
                }
                reader.close();
                Log.d(TAG, "城市数据加载成功，共 " + cityPinyinToIdMap.size() + " 条记录。");
            } catch (IOException e) {
                Log.e(TAG, "加载城市数据失败: " + e.getMessage());
                throw new RuntimeException("Failed to load city data from assets: " + e.getMessage(), e);
            }
        }
    }

    /**
     * 将中文城市名转换为拼音（全小写，无声调）。
     *
     * @param chineseCityName 中文城市名
     * @return 拼音字符串
     */
    private static String getPinyin(String chineseCityName) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);

        StringBuilder pinyinBuilder = new StringBuilder();
        try {
            for (char c : chineseCityName.toCharArray()) {
                if (c > 128) { // 是中文字符
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        pinyinBuilder.append(pinyinArray[0]); // 取第一个拼音
                    }
                } else {
                    pinyinBuilder.append(c); // 非中文字符直接添加
                }
            }
            Log.i(TAG, chineseCityName + "转换为拼音成功: " + pinyinBuilder);
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            Log.e(TAG, "拼音转换格式错误: " + e.getMessage());
            return null;
        }
        return pinyinBuilder.toString();
    }

    /**
     * 爬取指定城市的景点信息。
     * 此方法执行网络操作，必须在后台线程中调用。
     *
     * @param context 应用上下文
     * @param chineseCityName 中文城市名（例如："重庆"）
     * @return 一个 CompletableFuture，它将完成一个包含 Destination 对象的列表，或在失败时抛出异常。
     */
    public static CompletableFuture<List<Destination>> scrapeCityDestinations(Context context, String chineseCityName) {
        return CompletableFuture.supplyAsync(() -> {
            if (cityPinyinToIdMap == null || cityPinyinToIdMap.isEmpty()) {
                initCityData(context);
            }

            String pinyinCityName = getPinyin(chineseCityName);
            if (pinyinCityName == null || !cityPinyinToIdMap.containsKey(pinyinCityName)) {
                Log.e(TAG, "无法找到城市ID或拼音转换失败: " + chineseCityName);
                throw new IllegalArgumentException("无法找到指定城市的拼音或ID: " + chineseCityName);
            }

            String cityId = cityPinyinToIdMap.get(pinyinCityName);
            // Referer URL 现在根据 Python `settings.py` 中的 `referer` 字段来设置，其值为 `https://you.ctrip.com/`
            // 但如果实际请求的 Referer 动态变化，这可能需要更精细的控制
            String refererUrl = "https://you.ctrip.com/";

            // API URL 现在使用 Python settings.py 中定义的 URL
            String apiUrl = "https://m.ctrip.com/restapi/soa2/18109/json/getAttractionList";
            Log.d(TAG, "尝试请求API: " + apiUrl + ", Referer: " + refererUrl);

            List<Destination> destinations = new ArrayList<>();
            try {
                // 构建 JSON 请求体，完全匹配 Python settings.json_data 的结构
                JSONObject head = new JSONObject();
                head.put("cid", "09031076110042895602");
                head.put("ctok", "");
                head.put("cver", "1.0");
                head.put("lang", "01");
                head.put("sid", "8888");
                head.put("syscode", "999");
                head.put("auth", "");
                head.put("xsid", "");
                head.put("extension", new JSONArray()); // extension 是一个空数组

                JSONObject jsonPayload = new JSONObject();
                jsonPayload.put("head", head);
                jsonPayload.put("scene", "online");
                jsonPayload.put("districtId", Integer.parseInt(cityId)); // 使用 cityId 对应 districtId
                jsonPayload.put("index", 1); // 假设只爬取第一页
                jsonPayload.put("sortType", 1);
                jsonPayload.put("count", 20); // Python中最大是20个，正常每页10个

                // 如果 Python 代码中还有其他 params，例如 x-traceID, _fxpcqlniredt
                // 这些可能作为查询参数 (URL?param=value) 或自定义头部发送
                // 这里我们假设它们不是请求体的一部分，因为 Python settings.py 将其列为 params
                // 如果需要，可以在 Request.Builder().url(apiUrl + "?_fxpcqlniredt=...&x-traceID=...") 中添加

                RequestBody body = RequestBody.create(jsonPayload.toString(), JSON_MEDIA_TYPE);

                Request request = new Request.Builder()
                        .url(apiUrl)
                        .post(body)
                        // 从 Python settings.py 中复制的 Headers
                        .addHeader("accept", "*/*")
                        .addHeader("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6")
                        .addHeader("content-type", "application/json")
                        .addHeader("cookieorigin", "https://you.ctrip.com") // 确保这个头被发送
                        .addHeader("origin", "https://you.ctrip.com")
                        .addHeader("priority", "u=1, i")
                        .addHeader("referer", refererUrl) // 使用新的 refererUrl
                        .addHeader("sec-ch-ua", "\"Chromium\";v=\"130\", \"Microsoft Edge\";v=\"130\", \"Not?A_Brand\";v=\"99\"")
                        .addHeader("sec-ch-ua-mobile", "?0")
                        .addHeader("sec-ch-ua-platform", "\"Windows\"")
                        .addHeader("sec-fetch-dest", "empty")
                        .addHeader("sec-fetch-mode", "cors")
                        .addHeader("sec-fetch-site", "same-site")
                        .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36") // 使用与Python UserAgent().edge 类似的值

                        // 手动添加所有 Cookies 字符串
                        .addHeader("Cookie", PYTHON_COOKIES_STRING)
                        .build();

                // 记录完整的请求信息以便调试
                Log.d(TAG, "--- Sending Request ---");
                Log.d(TAG, "URL: " + request.url());
                Log.d(TAG, "Method: " + request.method());
                Log.d(TAG, "Headers: ");
                for (String name : request.headers().names()) {
                    Log.d(TAG, "  " + name + ": " + request.header(name));
                }
                if (request.body() != null) {
                    try {
                        Buffer buffer = new Buffer();
                        request.body().writeTo(buffer);
                        Log.d(TAG, "Body: " + buffer.readUtf8());
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading request body for logging: " + e.getMessage());
                    }
                }
                Log.d(TAG, "----------------------");

                try (Response response = httpClient.newCall(request).execute()) {
                    String responseBody = response.body().string();
                    if (!response.isSuccessful()) {
                        Log.e(TAG, "API请求失败: " + response.code() + " " + response.message() + ", 响应体: " + responseBody);
                        throw new IOException("API请求失败: " + response.code() + " " + response.message());
                    }

                    JSONObject jsonResponse = new JSONObject(responseBody);

                    // 检查JSON结构，Python中是 response_json.get("attractionList")
                    // 但这里可能是 {"data": {"attractionList": [...]}} 或其他嵌套
                    JSONArray attractionListJson = null;
                    if (jsonResponse.has("attractionList")) { // 直接检查根级的attractionList
                        attractionListJson = jsonResponse.getJSONArray("attractionList");
                    } else if (jsonResponse.has("data")) { // 或者在data字段下
                        JSONObject dataObject = jsonResponse.getJSONObject("data");
                        if (dataObject.has("attractionList")) {
                            attractionListJson = dataObject.getJSONArray("attractionList");
                        }
                    }

                    if (attractionListJson == null || attractionListJson.length() == 0) {
                        Log.w(TAG, "API返回数据中未找到景点列表或列表为空。URL: " + apiUrl + ", 响应: " + responseBody);
                        return Collections.emptyList();
                    }

                    for (int i = 0; i < attractionListJson.length(); i++) {
                        JSONObject attractionData = attractionListJson.getJSONObject(i).getJSONObject("card");

                        String name = attractionData.optString("poiName", "");
                        // detailPageUrl 由 cityPinyin, cityId 和 businessId 拼接而成
                        // 首先获取 businessId
                        String businessId = attractionData.optString("businessId", "");
                        String generatedDetailPageUrl = "https://you.ctrip.com/sight/" + pinyinCityName + cityId + "/" + businessId + ".html";
                        String detailPageUrl = generatedDetailPageUrl; // 确保使用生成后的URL

                        String zoneName = attractionData.optString("zoneName", "");
                        String addressDistance = zoneName;

                        int price = 0;
                        boolean isFree = attractionData.optBoolean("isFree", false);
                        if (isFree) {
                            price = 0;
                        } else {
                            double marketPrice = attractionData.optDouble("marketPrice", -1.0);
                            if (marketPrice >= 0) {
                                price = (int) marketPrice;
                            } else {
                                price = -1;
                            }
                        }

                        // 新的 description 和 tagNames 解析逻辑
                        String description = ""; // This will store the content from [\"..."]
                        List<String> tagNames = new ArrayList<>(); // This will store all other descriptive tags

                        String shortFeatures = attractionData.optString("shortFeatures", "");
                        if (!shortFeatures.isEmpty()) {
                            // 改进的提取description和tagNames逻辑
                            if (shortFeatures.length() >= 4 &&
                                shortFeatures.charAt(0) == '[' &&
                                shortFeatures.charAt(1) == '"' &&
                                shortFeatures.charAt(shortFeatures.length() - 2) == '"' &&
                                shortFeatures.charAt(shortFeatures.length() - 1) == ']') {
                                // 找到 ["..."] 模式作为description
                                description = shortFeatures.substring(2, shortFeatures.length() - 2); // 提取引号内的内容
                                String remainingFeatures = shortFeatures.substring(shortFeatures.length()).trim(); // ]之后的内容

                                // 解析 ] 之后的内容作为tagNames
                                if (!remainingFeatures.isEmpty()) {
                                    String[] parts = remainingFeatures.split("；");
                                    for (String part : parts) {
                                        if (!part.trim().isEmpty()) {
                                            tagNames.add(part.trim());
                                        }
                                    }
                                }
                            } else {
                                // 没有 ["..."] 模式，将整个 shortFeatures 作为tagNames
                                String[] parts = shortFeatures.split("；");
                                for (String part : parts) {
                                    if (!part.trim().isEmpty()) {
                                        tagNames.add(part.trim());
                                    }
                                }
                            }
                        }

                        // Add tags from tagNameList JSON array
                        JSONArray tagNamesJson = attractionData.optJSONArray("tagNameList");
                        if (tagNamesJson != null) {
                            for (int j = 0; j < tagNamesJson.length(); j++) {
                                String tagName = tagNamesJson.getString(j);
                                if (!tagName.trim().isEmpty()) {
                                    tagNames.add(tagName.trim());
                                }
                            }
                        }

                        // Add rankDescText to tagNames
                        String rankDescText = "";
                        JSONObject sightCategoryInfo = attractionData.optJSONObject("sightCategoryInfo");
                        if (sightCategoryInfo != null) {
                             rankDescText = sightCategoryInfo.optString("rankDescText", "");
                        }
                        if (!rankDescText.isEmpty()) {
                            tagNames.add(rankDescText);
                        }

                        double commentScore = attractionData.optDouble("commentScore", 0.0);
                        String sightLevel = attractionData.optString("sightLevelStr", "");
                        String coverImageUrl = attractionData.optString("coverImageUrl", "");
                        JSONObject coordinate = attractionData.optJSONObject("coordinate");
                        double latitude = 0.0;
                        double longitude = 0.0;
                        if (coordinate != null) {
                            latitude = coordinate.optDouble("latitude", 0.0);
                            longitude = coordinate.optDouble("longitude", 0.0);
                        }
                        double heatScore = attractionData.optDouble("heatScore", 0.0);

                        Destination dest = new Destination(
                                name, detailPageUrl, addressDistance, price, description, 
                                businessId, commentScore, sightLevel, coverImageUrl, tagNames,
                                latitude, longitude, heatScore,
                                pinyinCityName, cityId
                        );
                        destinations.add(dest);
                        Log.d(TAG, "爬取景点信息: " + dest.toString());
                    }
                }

                Log.d(TAG, "爬取完成，共找到 " + destinations.size() + " 个景点。");
                return destinations;

            } catch (IOException e) {
                Log.e(TAG, "网络请求失败: " + e.getMessage(), e);
                throw new RuntimeException("网络请求失败: " + e.getMessage(), e);
            } catch (JSONException e) {
                Log.e(TAG, "JSON解析失败: " + e.getMessage(), e);
                throw new RuntimeException("JSON解析失败: " + e.getMessage(), e);
            } catch (Exception e) {
                Log.e(TAG, "爬取过程中发生未知错误: " + e.getMessage(), e);
                throw new RuntimeException("爬取过程中发生未知错误", e);
            }
        });
    }

    /**
     * 景点数据模型类。
     * 更新以包含更多从JSON获取的字段。
     */
    public static class Destination {
        public String name;
        public String detailPageUrl;
        public String addressDistance;
        public int price;
        public String description; // This will now hold the content from [\"..."]
        public String businessId;
        public double commentScore;
        public String sightLevel;
        public String coverImageUrl;
        public List<String> tagNames; // This will hold all other descriptive tags
        public double latitude;
        public double longitude;
        public double heatScore;
        public String cityPinyin;
        public String cityId;

        public Destination(String name, String detailPageUrl, String addressDistance, int price, String description,
                           String businessId, double commentScore, String sightLevel, String coverImageUrl,
                           List<String> tagNames, double latitude, double longitude, double heatScore,
                           String cityPinyin, String cityId) { // Removed slogan parameter
            this.name = name;
            this.detailPageUrl = detailPageUrl;
            this.addressDistance = addressDistance;
            this.price = price;
            this.description = description;
            this.businessId = businessId;
            this.commentScore = commentScore;
            this.sightLevel = sightLevel;
            this.coverImageUrl = coverImageUrl;
            this.tagNames = tagNames != null ? tagNames : Collections.emptyList();
            this.latitude = latitude;
            this.longitude = longitude;
            this.heatScore = heatScore;
            this.cityPinyin = cityPinyin;
            this.cityId = cityId;
        }

        @Override
        public String toString() {
            return "Destination{" +
                    "name='" + name + '\'' +
                    ", detailPageUrl='" + detailPageUrl + '\'' +
                    ", addressDistance='" + addressDistance + '\'' +
                    ", price=" + price +
                    ", description='" + description + '\'' +
                    ", businessId='" + businessId + '\'' +
                    ", commentScore=" + commentScore +
                    ", sightLevel='" + sightLevel + '\'' +
                    ", coverImageUrl='" + coverImageUrl + '\'' +
                    ", tagNames=" + tagNames +
                    ", latitude=" + latitude +
                    ", longitude=" + longitude + '\'' +
                    ", heatScore=" + heatScore +
                    ", cityPinyin='" + cityPinyin + '\'' +
                    ", cityId='" + cityId + '\'' +
                    '}';
        }
    }
}