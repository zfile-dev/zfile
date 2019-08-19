var result = {};

var LOCAL_STORAGE_KEY = "zadmin-config";

$(document).ready(function () {

    /**
     * 弹出/收缩左侧侧边栏
     */
    $("#sidebar-toggle").on("click", function (e) {
        var sidebar = $("#sidebar");
        sidebar.toggleClass('hidden');
        updateConfig('sidebarIsVisible', !sidebar.hasClass('hidden'));
        $(this).find("img").attr('src', sidebar.hasClass('hidden') ?  '/h5ai/public/images/ui/sidebar.svg' : '/h5ai/public/images/ui/back.svg');
    });

    /**
     * 切换视图动作
     */
    $("#viewmode-settings").on("click", ".button",function (e) {
        $(this).siblings().removeClass('active');
        $(this).addClass('active');
        var className = $(this).data('class');
        $("#view").removeClass("view-details view-grid view-icons").addClass('view-' + className);
        updateConfig('mode', className);
    });

    /**
     * 右侧信息框显隐
     */
    $("#view-info").on("click", function (e) {
        $(this).toggleClass("active");
        infoObj.toggleClass('hidden');
        updateConfig('infoEnable', !infoObj.hasClass('hidden'));
    });

    /**
     * 列表大小
     */
    $("#viewmode-size").change(function(e) {
        var value = e.target.value;
        updateListSize(value);
        updateConfig('listSize', value);
    });

    // 二维码对象
    var qrcode = new QRCode('qrcode', {
        text: window.location.href,
        width: 180,
        height: 180,
        colorDark: '#999999',
        colorLight: '#ffffff',
        correctLevel: QRCode.CorrectLevel.H
    });

    /**
     * 列表鼠标悬浮, 加载右侧信息框
     */
    listObj.on('mouseenter', 'li', function (e) {
        var name = $(e.currentTarget).find('.name').html();
        var time = $(e.currentTarget).find('.time').html();
        var size = $(e.currentTarget).find('.size').html();
        var path = $(e.currentTarget).attr("data");
        var url = $(e.currentTarget).find("a")[0].href;
        var fileIcon = $(e.currentTarget).data('type') === 'folder' ? "h5ai/public/images/themes/default/folder.svg" : getFileType(path).fileIcon;
        buildInfo({name: name, time: time, path: path, url: url, size: size, fileIcon: fileIcon})
    });

    listObj.on('mouseleave', 'li', function (e) {
        var arr = getDirectoryList();
        var name = arr[arr.length - 1].text;
        var time = '';
        var size = '';
        var path = arr[arr.length - 1].path;
        var url = window.location.protocol + "//" + window.location.host + arr[arr.length - 1].path;
        var fileIcon = "h5ai/public/images/themes/default/folder.svg";
        if (name === '/') {
            name = window.location.host;
        }

        buildInfo({name: name, time: time, path: path, url: url, size: size, fileIcon: fileIcon})
    });


    /**
     * 构建右侧文件信息栏
     */
    function buildInfo(info) {
        $("#info .block .name").html(info.name);
        $("#info .block .time").html(info.time);
        qrcode.makeCode(info.url);
        $("#info-icon").attr("src", info.fileIcon);

        if (info.size !== '-') {
            $("#info .block .size").removeClass("hidden").html(info.size);
        } else {
            $("#info .block .size").addClass("hidden")
        }
    }

    /**
     * 绑定右键菜单
     */
    $.contextMenu({
        selector: '.item',
        callback: function(key, options) {
            var downloadUrl = options.$trigger.find('a').attr('href');
            window.open(downloadUrl);
        },
        items: {
            "download": {name: "下载", icon: "edit"}
        }
    });

});

/**
 * 加载配置项, 获取当前的
 * @param path
 */
function buildConfig(path) {
    path = path ? path : '';

    $.ajax({
        type: 'GET',
        url: 'getConfig',
        data: {
            path: path
        },
        dataType: 'json',
        success: function (data) {
            result = data.data;
            var systemConfig = result.systemConfig;

            var systemConfigCache = localStorage.getItem(LOCAL_STORAGE_KEY);
            if (systemConfigCache) {
                systemConfig = JSON.parse(systemConfigCache);
            }

            // 构建标题
            $(document).attr("title", systemConfig.siteName);

            // 构建页头
            if (result.header) {
                var headerMarkdown = marked(result.header); // 解析 markdown
                $("#header").html(headerMarkdown);
            } else {
                $("#header").html('');
            }

            // 构建页尾
            if (result.footer) {
                var footerMarkdown = marked(result.footer); // 解析 markdown
                $("#footer").html(footerMarkdown);
            } else {
                $("#footer").html('');
            }



            if (systemConfig.infoEnable) {
                $("#info").removeClass('hidden');
                $("#view-info").addClass('active');
            } else {
                $("#info").addClass('hidden');
            }

            if (systemConfig.searchEnable) {
                $("#search").removeClass('hidden');
            } else {
                $("#search").addClass('hidden');
            }

            if (systemConfig.sidebarEnable) {
                $("#sidebar-toggle").removeClass('hidden');
            } else {
                $("#sidebar-toggle").addClass('hidden');
            }

            if (systemConfig.sidebarIsVisible) {
                $("#sidebar").removeClass('hidden');
                $("#sidebar-toggle").find("img").attr('src', '/h5ai/public/images/ui/back.svg');
            }

            if (systemConfig.mode) {
                $("#view").removeClass('view-details view-grid view-icons').addClass('view-' + systemConfig.mode.toLowerCase());
                $("#viewmode-details").siblings().removeClass('active');
                $("#viewmode-" + systemConfig.mode.toLowerCase()).addClass('active');
            }

            if (systemConfig.listSize) {
                updateListSize(systemConfig.listSize);
                $("#viewmode-size").val(systemConfig.listSize);
            }

            localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(systemConfig));
        },
        error: function (textStatus, errorThrown) {
            alert("加载站点配置失败.!");
            console.error(XMLHttpRequest.status);
            console.error(XMLHttpRequest.readyState);
            console.error(textStatus);
        }
    });
}

function updateConfig(key, value) {
    var systemConfigStr = localStorage.getItem(LOCAL_STORAGE_KEY);
    var systemConfig = JSON.parse(systemConfigStr);
    systemConfig[key] = value;
    localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(systemConfig));
}

function updateListSize(value) {
    var size;
    if (value < 6) {
        size = value * 20;
    } else {
        size = 100 + (value - 5) * 40;
    }
    var classArr = $("#view").attr('class').split(" ");

    $.each(classArr, function (i, val) {
        if (val.indexOf('view-size-') > -1) {
            $("#view").removeClass(val).addClass("view-size-" + size);
            return false;
        }
    })
}