requirejs.config({
    paths: {
        jquery: '/script/jquery-3.3.1.min',
        zfile: '/script/main',
        Mustache: '/mustache/mustache.min',
        index: '/script/index',
        QRCode: '/qrcode/qrcode',
        contextMenu: '/contextMenu/jquery.contextMenu',
        marked: '/marked/marked.min',
        layer: '/layer/layer',
        highlight: '/highlight/highlight.min',
        DPlayer: '/DPlayer/DPlayer.min',
        Shikwasa: '/shikwasa/shikwasa.min',
        swal: '/sweetalert/sweetalert.min'
    },
    shim: {
        zfile: {
            exports: 'zfile'
        },
        Mustache: {
            exports: 'Mustache'
        },
        index: {
            deps: ['zfile'],
            exports: 'index'
        },
        QRCode: {
            exports: 'QRCode'
        },
        Shikwasa: {
            exports: 'Shikwasa'
        }
    }
});

requirejs(['index'], function(index) {
});