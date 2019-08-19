$(document).ready(function () {

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