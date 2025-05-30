function getQueryVariable(variable) {
    var query = window.location.search.substring(1);
    var vars = query.split("&");
    for (var i = 0; i < vars.length; i++) {
        var pair = vars[i].split("=");
        if (pair[0] == variable) { return decodeURIComponent(pair[1]); }
    }
    return (null);
}
var city = getQueryVariable("city") || "北京";
// 修改城市名颜色
var cityTitle = document.getElementById('city-title');
cityTitle.innerHTML = '你正在前往：<span style="color:#007A8C;">' + city + '</span>';

var map = new AMap.Map('container', {
    resizeEnable: true,
    zoom: 13,
    viewMode: '3D',
    mapStyle: 'amap://styles/normal'
});

// 添加简易缩放控件
AMap.plugin('AMap.ToolBar', function () {
    map.addControl(new AMap.ToolBar({ liteStyle: true }));
});

// 高亮城市边界
AMap.plugin('AMap.DistrictSearch', function () {
    var district = new AMap.DistrictSearch({
        extensions: 'all',
        level: 'city'
    });
    district.search(city, function (status, result) {
        if (status === 'complete' && result.districtList && result.districtList[0]) {
            var bounds = result.districtList[0].boundaries;
            if (bounds && bounds.length) {
                for (var i = 0; i < bounds.length; i++) {
                    new AMap.Polygon({
                        map: map,
                        path: bounds[i],
                        strokeColor: "#4285f4",
                        strokeWeight: 3,
                        fillColor: "#90caf9",
                        fillOpacity: 0.18
                    });
                }
                // 自动适配视野
                map.setFitView();
            }
        }
    });
});

// 分类icon和颜色配置
const categoryConfig = {
    '旅游景点': {
        icon: 'icons/ic_marker_spot.svg',
        size: [24, 24]
    },
    '美食': {
        icon: 'icons/ic_marker_food.svg',
        size: [24, 24]
    },
    '酒店': {
        icon: 'icons/ic_marker_hotel.svg',
        size: [24, 24]
    },
    '网红打卡点': {
        icon: 'icons/ic_marker_hotpot.svg',
        size: [24, 24]
    },
    'default': {
        icon: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png',
        size: [24, 24]
    }
};

// marker图标统一用官方默认icon
function getMarkerIconByCategory(category) {
    const dpr = window.devicePixelRatio || 1;
    let conf = categoryConfig[category] || categoryConfig['default'];
    let size = conf.size.slice(); // 使用slice()创建副本，避免修改原始配置
    if (dpr >= 3) size = size.map(v => Math.round(v * 1.3));
    else if (dpr >= 2) size = size.map(v => Math.round(v * 1.15));
    return {
        icon: new AMap.Icon({ image: conf.icon, size: new AMap.Size(...size), imageSize: new AMap.Size(...size) }),
        size: size
    };
}

// marker管理
let markers = [];
function clearMarkers() {
    markers.forEach(m => map.remove(m));
    markers = [];
}

// marker和卡片
let currentMarker = null;
function showInfoCard(poi, marker) {
    let photoHtml = '';
    if (poi.photos && poi.photos.length > 0) {
        photoHtml = `<img src="${poi.photos[0].url}" style="max-width:90vw;max-height:80px;margin-top:4px;">`;
    }
    document.getElementById('cardContent').innerHTML = `
        <button class="info-close-btn" onclick="hideInfoCard()">×</button>
        <h3 class="info-title">${poi.name}</h3>
        <div class="info-address">${poi.address || ''}</div>
        ${photoHtml}
        <div class="info-detail">
            ${poi.tel ? `电话: ${poi.tel}<br>` : ''}
            类型: ${poi.type}<br>
            ${poi.distance ? `距离: ${(poi.distance / 1000).toFixed(1)}公里<br>` : ''}
        </div>
        <button class="info-nav-btn" onclick="window.planRouteTo('${poi.location.lng},${poi.location.lat}')">导航到这里</button>
    `;
    document.getElementById('infoCard').classList.add('active');
    map.setCenter(poi.location);
    currentMarker = marker;
}
function hideInfoCard() {
    document.getElementById('infoCard').classList.remove('active');
    currentMarker = null;
}
map.on('click', () => { hideInfoCard(); });

// 动态缩放marker
map.on('zoomchange', () => {
    const zoom = map.getZoom();
    const baseSizeWidth = 32; // 基于categoryConfig中size[0]的基准宽度
    const baseSizeHeight = 40; // 基于categoryConfig中size[1]的基准高度
    const scaleFactor = Math.max(0.8, Math.min(1.7, zoom / 13));

    markers.forEach(marker => {
        const icon = marker.getIcon();
        if (icon && typeof icon.getImageSize === 'function') { // 确保是AMap.Icon对象
            // 尝试从marker本身获取原始尺寸信息，如果没有，则使用默认基准
            let originalSize = marker.getExtData()?.originalIconSize || [baseSizeWidth, baseSizeHeight];

            const newWidth = originalSize[0] * scaleFactor;
            const newHeight = originalSize[1] * scaleFactor;

            icon.setSize(new AMap.Size(newWidth, newHeight));
            icon.setImageSize(new AMap.Size(newWidth, newHeight)); // 通常size和imageSize一致
            marker.setIcon(icon);
            // 更新marker的偏移量以保持底部中心对齐
            marker.setOffset(new AMap.Pixel(0, -newHeight / 2));
        }
    });
});


// 搜索功能
let cityCenter = null;
AMap.plugin(['AMap.Geocoder', 'AMap.PlaceSearch'], function () {
    var geocoder = new AMap.Geocoder();
    geocoder.getLocation(city, function (status, result) {
        if (status === 'complete' && result.geocodes.length) {
            cityCenter = result.geocodes[0].location;
            map.setZoomAndCenter(13, [cityCenter.lng, cityCenter.lat]);
            fetchDefaultPOIs();
        } else {
            console.error('城市地理编码失败:', result);
            alert('无法获取城市位置信息，请检查城市名称或网络。');
        }
    });
});

// 默认四类POI
const defaultCategories = [
    { keyword: "旅游景点" },
    { keyword: "美食" },
    { keyword: "酒店" },
    { keyword: "网红打卡点" }
];
function fetchDefaultPOIs() {
    if (!cityCenter) return;
    clearMarkers();
    let allPois = [];
    let loaded = 0;
    let idSet = new Set(); // 用于去重
    defaultCategories.forEach(cat => {
        var placeSearch = new AMap.PlaceSearch({
            city: city,
            type: '', // 类别为空，依靠keyword搜索
            location: cityCenter, // 使用高德API的LngLat对象
            radius: 8000,
            pageSize: 15
        });
        placeSearch.search(cat.keyword, function (status, result) {
            loaded++;
            if (status === 'complete' && result.poiList && result.poiList.pois) {
                result.poiList.pois.forEach(poi => {
                    if (!idSet.has(poi.id)) {
                        poi._category = cat.keyword;
                        allPois.push(poi);
                        idSet.add(poi.id);
                    }
                });
            } else if (status === 'error') {
                console.error(`搜索 '${cat.keyword}' 失败:`, result);
            }
            if (loaded === defaultCategories.length) {
                addPois(allPois);
            }
        });
    });
}

// 关键词搜索
function doKeywordSearch(keyword) {
    if (!cityCenter) return;
    clearMarkers();
    var placeSearch = new AMap.PlaceSearch({
        city: city,
        type: '',
        location: cityCenter,
        radius: 8000,
        pageSize: 20
    });
    placeSearch.search(keyword, function (status, result) {
        if (status === 'complete' && result.poiList && result.poiList.pois) {
            result.poiList.pois.forEach(poi => poi._category = 'default'); // 标记为默认类别
            addPois(result.poiList.pois);
        } else {
            console.error(`关键词搜索 '${keyword}' 失败或无结果:`, result);
            if (result.poiList.pois.length === 0) alert("未搜索到相关地点");
        }
    });
}
// 周边搜索
function doAroundSearch(keyword) {
    if (!cityCenter) return;
    clearMarkers();
    var placeSearch = new AMap.PlaceSearch({
        city: city, // 周边搜索通常也需要指定城市以提高准确性
        type: '',
        // location: cityCenter, // searchNearBy的第一个参数就是中心点
        radius: 8000, // searchNearBy的第三个参数是半径
        pageSize: 20
    });
    placeSearch.searchNearBy(keyword, cityCenter, 8000, function (status, result) {
        if (status === 'complete' && result.poiList && result.poiList.pois) {
            result.poiList.pois.forEach(poi => poi._category = 'default');
            addPois(result.poiList.pois);
        } else {
            console.error(`周边搜索 '${keyword}' 失败或无结果:`, result);
            if (result.poiList.pois.length === 0) alert("未搜索到周边相关地点");
        }
    });
}
// 添加POI到地图
function addPois(pois) {
    if (!pois || pois.length === 0) {
        console.log("没有POI数据可以添加到地图。");
        // 可以选择给用户一个提示，例如: alert("未找到相关地点。");
        map.setFitView(); // 如果之前有marker，现在清空了，也需要调整视野
        return;
    }
    pois.forEach(function (poi) {
        let cat = poi._category || 'default';
        let { icon, size: iconOriginalSize } = getMarkerIconByCategory(cat); // iconOriginalSize 是配置中的原始尺寸
        var marker = new AMap.Marker({
            position: poi.location,
            title: poi.name,
            map: map,
            icon: icon,
            offset: new AMap.Pixel(0, -iconOriginalSize[1] / 2) // 使用原始图标高度的一半作为偏移
        });
        // 存储原始图标尺寸，用于动态缩放
        marker.setExtData({ originalIconSize: iconOriginalSize });

        marker.setLabel({
            direction: 'bottom',
            offset: new AMap.Pixel(0, 8), // label的偏移，根据需要调整
            content: `<div class="marker-label">${poi.name}</div>` // 显示POI名称作为label
        });
        marker.on('click', function () {
            showInfoCard(poi, marker);
        });
        marker.on('dblclick', function () {
            map.setCenter(marker.getPosition());
            map.setZoom(Math.min(map.getZoom() + 2, 18));
            showInfoCard(poi, marker);
        });
        markers.push(marker);
    });
    if (markers.length) {
        map.setFitView(markers);
    } else {
        map.setCenter(cityCenter || map.getCenter()); // 如果没有marker，居中到城市中心或当前地图中心
        map.setZoom(13); // 设置一个默认的缩放级别
    }
}

// 搜索按钮事件
document.getElementById('search-btn').onclick = function () {
    var kw = document.getElementById('search-input').value.trim();
    if (kw) {
        doKeywordSearch(kw);
    } else {
        alert("请输入关键词！");
    }
};
document.getElementById('around-btn').onclick = function () {
    var kw = document.getElementById('search-input').value.trim() || '风景名胜'; // 如果为空，默认搜索风景名胜
    doAroundSearch(kw);
};

// 导航功能 (确保在AMap加载完成后调用)
window.planRouteTo = function (destStr) {
    let [lng, lat] = destStr.split(',').map(Number);
    AMap.plugin('AMap.Geolocation', function () {
        var geolocation = new AMap.Geolocation({
            enableHighAccuracy: true, // 是否使用高精度定位，默认false
            timeout: 10000, // 超过10秒后停止定位，默认无穷大
        });
        geolocation.getCurrentPosition(function (status, result) {
            if (status === 'complete' && result.position) {
                let start = result.position; // LngLat对象
                AMap.plugin('AMap.Driving', function () {
                    var driving = new AMap.Driving({ map: map, panel: "panel" }); // panel可选，用于显示路线详情
                    // 清除之前的路线规划（如果需要）
                    // driving.clear();
                    driving.search(new AMap.LngLat(start.lng, start.lat), new AMap.LngLat(lng, lat), function (drivingStatus, drivingResult) {
                        if (drivingStatus === 'complete') {
                            console.log('驾车路线规划完成');
                        } else {
                            console.error('驾车路线规划失败', drivingResult);
                            alert('路线规划失败，请稍后再试。');
                        }
                    });
                });
            } else {
                console.error('定位失败:', result);
                alert('定位失败，请检查定位权限或网络后重试。');
            }
        });
    });
};

// 初始化时，如果URL中没有city参数，可以尝试IP定位获取当前城市
// 这部分代码可以在DOM加载完成后执行
document.addEventListener('DOMContentLoaded', function () {
    if (!getQueryVariable("city")) {
        AMap.plugin('AMap.Geolocation', function () {
            var geolocation = new AMap.Geolocation({
                enableHighAccuracy: true,
                timeout: 10000,
                extensions: 'all' //返回地址信息及附近POI、道路等信息，默认'base'
            });
            geolocation.getCurrentPosition(function (status, result) {
                if (status == 'complete' && result.addressComponent && result.addressComponent.city) {
                    city = result.addressComponent.city;
                    document.getElementById('city-title').textContent = "你正在前往：" + city;
                    // 后续的地图初始化和POI加载逻辑会自动使用这个更新后的city
                    // 如果getLocation已经执行，可能需要重新触发
                    if (cityCenter) { // 如果cityCenter已由之前的getLocation获取，但我们现在有了更精确的城市
                        geocoder.getLocation(city, function (status, result) { // 重新获取新城市中心
                            if (status === 'complete' && result.geocodes.length) {
                                cityCenter = result.geocodes[0].location;
                                map.setZoomAndCenter(13, [cityCenter.lng, cityCenter.lat]);
                                fetchDefaultPOIs();
                            }
                        });
                    }
                } else {
                    console.log('IP定位失败或无法获取城市信息, 使用默认城市: 北京');
                }
            });
        });
    }
});