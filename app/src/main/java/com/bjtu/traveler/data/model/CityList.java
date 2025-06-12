package com.bjtu.traveler.data.model;

import java.util.Arrays;
import java.util.List;

public class CityList {
    // 热门城市列表（用于热门景点推荐）
    public static final List<String> HOT_CITIES = Arrays.asList(
        // 热门中国城市（增加更多城市）
        "北京市", "上海市", "广州市", "深圳市", "成都市", "杭州市", "重庆市",
        "西安市", "南京市", "武汉市", "天津市", "苏州市", "青岛市", "厦门市",
        "长沙市", "哈尔滨市", "昆明市", "大连市", "郑州市", "济南市", "合肥市",
        "福州市", "宁波市", "无锡市", "佛山市", "东莞市", "沈阳市", "长春市",
        "石家庄市", "太原市", "兰州市", "贵阳市", "南宁市", "海口市", "南昌市",
        "呼和浩特市", "乌鲁木齐市", "银川市", "西宁市", "拉萨市", "南通市", "徐州市",
        "常州市", "潍坊市", "烟台市", "洛阳市", "唐山市", "保定市", "临沂市",
        "金华市", "台州市", "嘉兴市", "绍兴市", "湖州市", "桂林市", "珠海市",
        "中山市", "惠州市", "江门市", "湛江市", "汕头市", "揭阳市", "茂名市",
        // 国际热门城市
        "New York", "London", "Paris", "Tokyo", "Singapore", "Dubai",
        "Sydney", "Seoul", "Los Angeles", "Berlin", "Rome", "Barcelona"
    );

    // 推荐景点用的城市/地区列表（更广泛，含省份、特别行政区等）
    public static final List<String> RECOMMEND_CITIES = Arrays.asList(
        // 核心城市（原列表）
        "北京市", "上海市", "广州市", "深圳市", "成都市", "杭州市", "重庆市",
        "西安市", "南京市", "武汉市", "天津市", "苏州市", "青岛市", "厦门市",
        "长沙市", "哈尔滨市", "昆明市", "大连市", "郑州市", "济南市", "合肥市",
        "福州市", "宁波市", "无锡市", "佛山市", "东莞市", "沈阳市", "长春市",
        "石家庄市", "太原市", "兰州市", "贵阳市", "南宁市", "海口市", "南昌市",
        "呼和浩特市", "乌鲁木齐市", "银川市", "西宁市", "拉萨市", "南通市", "徐州市",
        "常州市", "潍坊市", "烟台市", "洛阳市", "唐山市", "保定市", "临沂市",
        "金华市", "台州市", "嘉兴市", "绍兴市", "湖州市", "桂林市", "珠海市",
        "中山市", "惠州市", "江门市", "湛江市", "汕头市", "揭阳市", "茂名市",

        /* Ⅱ型大城市扩展 */
        "邯郸市", "珠海市", "包头市", "大同市", "赣州市", "西宁市", "扬州市",
        "遵义市", "襄阳市", "鞍山市", "昆山市", "莆田市", "绵阳市", "盐城市",
        "泉州市", "咸阳市", "芜湖市", "株洲市", "淮安市", "济宁市", "吉林市",
        "大庆市", "秦皇岛市", "湛江市", "宜昌市", "齐齐哈尔市", "抚顺市",
        "上饶市", "南充市", "邢台市", "泰安市", "开封市", "张家口市", "新乡市",
        "聊城市", "淮南市", "十堰市", "宜宾市", "枣庄市", "衡阳市", "长治市",
        "连云港市", "锦州市", "赤峰市", "晋江市", "泸州市",

        /* 百强城市补充 */
        "温州市", "常德市", "漳州市", "遵义市", "宜昌市", "沧州市", "衡水市",
        "柳州市", "三亚市", "威海市", "湘潭市", "泰州市", "镇江市", "蚌埠市",
        "东营市", "宿迁市", "宁德市", "龙岩市", "郴州市", "黄冈市", "韶关市",
        "清远市", "肇庆市", "宿州市", "滁州市", "南平市", "三明市", "莆田市",
        "晋中市", "运城市", "渭南市", "延安市", "平顶山市", "安阳市", "鹤壁市",
        "周口市", "黄石市", "荆州市", "邵阳市", "益阳市", "河源市", "阳江市",
        "梅州市", "潮州市", "汕尾市", "云浮市", "百色市", "梧州市", "玉林市",
        "河池市", "来宾市", "贺州市", "钦州市", "防城港市", "贵港市", "桂林市",
        "曲靖市", "宝鸡市", "天水市", "酒泉市", "中卫市", "克拉玛依市",
        "阿拉善盟", "巴彦淖尔市", "乌兰察布市", "鄂尔多斯市", "赤峰市", "通辽市",

        "New York", "London", "Paris", "Tokyo", "Singapore", "Dubai",
        "Sydney", "Seoul", "Los Angeles", "Berlin", "Rome", "Barcelona",
        "Istanbul", "Moscow", "Toronto", "Vancouver", "San Francisco",
        "Chicago", "Las Vegas", "Miami", "Bangkok", "Kuala Lumpur",
        "Mumbai", "Delhi", "Jakarta", "Manila", "Ho Chi Minh City"
//        "Cairo", "Cape Town", "Johannesburg", "Nairobi", "Rio de Janeiro",
//        "Sao Paulo", "Buenos Aires", "Mexico City", "Lima", "Santiago",
//        "Amsterdam", "Brussels", "Vienna", "Prague", "Madrid", "Lisbon",
//        "Athens", "Oslo", "Stockholm", "Helsinki", "Copenhagen", "Warsaw",
//        "Budapest", "Dublin", "Edinburgh", "Zurich", "Geneva", "Milan",
//        "Venice", "Florence", "Osaka", "Kyoto", "Seoul", "Taipei", "Hong Kong",
//        "Macau", "Manila", "Hanoi", "Phnom Penh", "Vientiane", "Ulaanbaatar",
//        "Colombo", "Dhaka", "Kathmandu", "Tehran", "Riyadh", "Doha",
//        "Abu Dhabi", "Muscat", "Tel Aviv", "Jerusalem", "Baghdad",
//        "Auckland", "Wellington", "Honolulu", "Reykjavik", "Havana",
//        "Kingston", "San Juan", "Bogota", "Caracas", "Quito", "La Paz"
    );
}

