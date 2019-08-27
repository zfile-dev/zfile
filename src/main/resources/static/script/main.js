define(['jquery', 'Mustache', 'layer', 'marked', 'highlight', 'DPlayer', 'Shikwasa'], function ($, Mustache, layer, marked, hljs, DPlayer, Shikwasa){

    var zfile = {
        prefixPath: '/file',
        nameObj: $(".name"),
        timeObj: $(".time"),
        sizeObj: $(".size"),
        infoObj: $("#info"),
        listObj: $("#list"),
        util: {
            bytesToSize: function (bytes) {
                if (bytes === 0) return '0 B';
                var k = 1024;
                sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
                i = Math.floor(Math.log(bytes) / Math.log(k));
                return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i];
            },
            getTime: function () {
                var ts = arguments[0] || 0;
                var t, y, m, d, h, i, s;
                t = ts ? new Date(ts * 1000) : new Date();
                y = t.getFullYear();
                m = t.getMonth() + 1;
                d = t.getDate();
                h = t.getHours();
                i = t.getMinutes();
                s = t.getSeconds();
                return y + '-' + (m < 10 ? '0' + m : m) + '-' + (d < 10 ? '0' + d : d) + ' ' + (h < 10 ? '0' + h : h) + ':' + (i < 10 ? '0' + i : i) + ':' + (s < 10 ? '0' + s : s);
            }
        },
        buildList: function (data) {
            var path = zfile.getPath();

            var list = [];
            $.each(data, function (i, val) {
                var fileType, url;
                var type = val.type.toLocaleLowerCase();
                if (type === 'file') {
                    fileType = zfile.getFileType(val.name);
                    url = zfile.getDownloadUrl(path + '/' + val.name);
                } else {
                    fileType = {
                        fileIcon: '/h5ai/public/images/themes/default/folder.svg',
                        fileType: 'folder'
                    };
                    url = zfile.removeDuplicateSeparator(zfile.prefixPath + '/' + path + '/' + val.name);
                }

                list[i] = {
                    path: path + '/' + val.name,
                    url: url,
                    time: val.time,
                    name: val.name,
                    size: type === 'folder' ?  '-' : zfile.util.bytesToSize(val.size),
                    icon: fileType.fileIcon,
                    type: fileType.fileType,
                    class: type
                };
            });

            var template = $('#listTemplate').html();
            Mustache.parse(template);
            var rendered = Mustache.render(template, {data: list});
            this.listObj.html(rendered);
        },
        getPath: function () {
            return window.location.pathname.substring(this.prefixPath.length);
        },
        listObjects: function (path, sortBy, descending) {
            // 如文件路径后没加 "/" , 则加上 "/".
            if (path !== '' && path.charAt(path.length - 1) !== '/') {
                path += '/';
            }

            // 如果没有指定排序方式, 则获取当前的排序方式.
            if (!sortBy) {
                sortBy = zfile.getSortBy();
            }

            if (descending === '') {
                descending = zfile.getDescending() === 'desc';
            }
            $("#items").attr("style", "opacity: 0.5;-moz-opacit: 0.5;");
            $.ajax({
                type: 'GET',
                url: '/api/list',
                data: {
                    path: encodeURIComponent(decodeURIComponent(path)),   // 先解码, 再编码, 防止重复编码
                    sortBy: sortBy,
                    descending: descending
                },
                dataType: 'json',
                success: function (result) {
                    var data = result.data;
                    if (data.length === 0) {
                        zfile.listObj.html('<div id="view-hint" class="l10n-empty">空文件夹</div>');
                    } else {
                        zfile.buildList(data);
                    }
                },
                error: function (textStatus) {
                    zfile.clearList();
                    alert("加载列表失败");
                    console.error(XMLHttpRequest.status);
                    console.error(XMLHttpRequest.readyState);
                    console.error(textStatus);
                },
                complete: function () {
                    zfile.buildBreadcrumb();
                    zfile.buildBack();
                    // if (window.location.pathname !== '/admin') {
                    //     index.buildConfig(zfile.getPath(), true);
                    // }
                    $("#items").attr("style", "opacity: 1.0;-moz-opacit: 1.0;");
                }
            });
        },
        clearList: function () {
            this.listObj.html('');
        },
        getDownloadUrl: function (path) {
            var result;
            $.ajax({
                type: 'GET',
                url: '/api/downloadUrl',
                data: {
                    path: encodeURIComponent(decodeURIComponent(path))
                },
                async: false,
                success: function (data) {
                    result = data.data;
                }
            });
            return result;
        },
        getImageInfo: function (url) {
            var result;
            $.ajax({
                type: 'GET',
                url: '/api/getImageInfo',
                data: {
                    url: encodeURIComponent(decodeURIComponent(url))
                },
                async: false,
                success: function (data) {
                    result = data.data;
                }
            });
            return result;
        },
        getSortBy: function () {
            if (zfile.nameObj.hasClass('ascending') || zfile.nameObj.hasClass('descending')) {
                return "name";
            }

            if (zfile.sizeObj.hasClass('ascending') || zfile.sizeObj.hasClass('descending')) {
                return "size";
            }

            if (zfile.timeObj.hasClass('ascending') || zfile.timeObj.hasClass('descending')) {
                return "time";
            }

            return 'name';
        },
        getDescending: function () {
            var obj = $(".name, .size, .time");
            if (obj.hasClass("descending")) {
                return 'desc';
            }

            if (obj.hasClass("ascending")) {
                return 'asc';
            }
        },
        sortSwitch: function (who) {
            if ($(who).hasClass("descending")) {
                this.listObjects(zfile.getPath(), zfile.getSortBy(), false);
                zfile.sortClear();
                $(who).addClass("ascending");
            } else {
                this.listObjects(zfile.getPath(), zfile.getSortBy(), true);		//默认新一次排序为倒序
                zfile.sortClear();
                $(who).addClass("descending");
            }
        },
        sortClear: function () {
            $(".header a").removeClass("ascending").removeClass("descending");
        },
        buildBreadcrumb: function () {
            var template = $('#crumbTemplate').html();
            Mustache.parse(template);
            var rendered = Mustache.render(template, {data: this.getDirectoryList().splice(1)});
            $("#crumbbar").html(rendered);
            $("#crumbbar a.crumb:last").attr("class", "crumb active");
        },
        buildBack: function () {
            var template = $('#backTemplate').html();
            Mustache.parse(template);
            var directoryList = zfile.getDirectoryList();
            if (directoryList.length > 1) {
                var backPath = directoryList[directoryList.length - 2];
                var rendered = Mustache.render(template, backPath);
                $("#back").html(rendered);
            } else {
                $("#back").html('');
            }
        },
        getDirectoryList: function () {
            var hash = zfile.getPath();

            // 如果 # 后第一个字符是 '/', 则删除空格
            hash = hash.charAt(0) === '/' ? hash.substr(1) : hash;

            var pathSplit = decodeURIComponent(hash).split("/");
            var directoryList = [{
                url: zfile.prefixPath,
                text: '/',
                path: '/'
            }];

            if (hash !== '') {
                $.each(pathSplit, function (i, val) {
                    directoryList[i + 1] = {
                        url: zfile.removeDuplicateSeparator(directoryList[i].url + '/' + val),
                        path: zfile.removeDuplicateSeparator(directoryList[i].path + '/' + val),
                        text: val
                    }
                });
            }
            return directoryList;
        },
        getFileType: function (fileName) {
            var result;
            var fileSuffix = fileName.substring(fileName.lastIndexOf('.') + 1).toLocaleLowerCase();
            var fileIcon;
            var fileType;
            switch (fileSuffix) {
                case 'avi': case 'wmv': case 'mpeg': case 'mp4': case 'mov': case 'mkv': case 'flv': case 'f4v': case 'm4v': case 'rmvb': case 'rm': case '3gp': case 'dat': case 'ts': case 'mts':
                    fileIcon = '/h5ai/public/images/themes/default/vid.svg';
                    fileType = 'video';
                    break;
                case 'bmp': case 'jpg': case 'png': case 'tiff': case 'gif': case 'pcx': case 'tga': case 'exif': case 'fpx': case 'svg': case 'psd': case 'cdr': case 'pcd': case 'dxf': case 'ufo': case 'eps': case 'ai': case 'raw': case 'wmf': case 'webp':
                    fileIcon = '/h5ai/public/images/themes/default/img.svg';
                    fileType = 'image';
                    break;
                case 'mp3': case 'wma': case 'ape': case 'flac': case 'aac': case 'ac3': case 'mmf': case 'amr': case 'm4a': case 'm4r': case 'ogg': case 'wav': case 'mp2':
                    fileIcon = '/h5ai/public/images/themes/default/aud.svg';
                    fileType = 'audio';
                    break;
                case 'zip': case 'rar': case '7z': case 'tar': case 'gz':
                    fileIcon = '/h5ai/public/images/themes/default/ar.svg';
                    fileType = 'compress';
                    break;
                case 'exe': case 'dll': case 'com': case 'vbs':
                    fileIcon = '/h5ai/public/images/themes/default/bin.svg';
                    fileType = 'executable';
                    break;
                case 'apk': case 'deb': case 'rpm':
                    fileIcon = '/h5ai/public/images/themes/comity/ar-' + fileSuffix + '.svg';
                    fileType = 'ar';
                    break;
                case 'md':
                    fileIcon = '/h5ai/public/images/themes/comity/txt-' + fileSuffix + '.svg';
                    fileType = 'markdown';
                    break;
                case 'css': case 'go': case 'html': case 'js': case 'less': case 'php': case 'py': case 'rb': case 'rust': case 'script':
                    fileIcon = '/h5ai/public/images/themes/comity/txt-' + fileSuffix + '.svg';
                    fileType = 'language';
                    break;
                case 'txt': case 'htm': case 'sh': case 'bat': case 'json': case 'xml':
                    fileIcon = '/h5ai/public/images/themes/default/txt.svg';
                    fileType = 'language';
                    break;
                case 'pdf':
                    fileIcon = '/h5ai/public/images/themes/comity/x-pdf.svg';
                    fileType = 'pdf';
                    break;
                default:
                    fileIcon = '/h5ai/public/images/themes/default/file.svg';
                    fileType = 'other';
            }
            result = {fileIcon: fileIcon, fileType: fileType};
            return result;
        },
        openVideo: function (url) {
            layer.open({
                type: 1,
                title: false,
                area: ['90%', '90%'],
                shade: 0.3,
                shadeClose: true,
                closeBtn: 2,
                anim: 5,
                content: '<div id="dplayer" style="width: 100%; height: 100%;"></div>'
            });

            new DPlayer({
                container: document.getElementById('dplayer'),
                video: {
                    url: url
                },
                autoplay: true
            });
        },
        openText: function (fileType, fileName, path) {
            layer.open({
                type: 1,
                title: fileName,
                area: ['90%', '90%'],
                shade: 0.3,
                shadeClose: true,
                closeBtn: 1,
                anim: 5,
                content: '<div id="markdown-content" class="hidden"></div><pre><code id="text-content" class="hidden"></code></pre>'
            });

            $.get('/api/getContent', {path: encodeURIComponent(decodeURIComponent(path))}, function (result) {
                var text = result.data;
                var markdownContent = $("#markdown-content");
                var textContent = $("#text-content");
                if (fileType === 'markdown') {
                    markdownContent.removeClass("hidden").html(marked(result.data));
                    textContent.addClass("hidden");
                } else {
                    textContent.removeClass("hidden").html(hljs.highlightAuto(text).value);
                    markdownContent.addClass("hidden");
                }

            });
        },
        openImage: function (url) {
            var imageObj = $(".item.file[data-type='image'] a");

            var data = [];
            var startIndex = 0;
            $.each(imageObj, function (i, val) {
                var href = $(val).attr('href');
                data.push({
                    "src": href
                });

                if (url === href) {
                    startIndex = i;
                }
            });

            var json = {
                "start": startIndex, //初始显示的图片序号，默认 0
                "data": data
            };

            layer.photos({
                photos: json
                ,anim: 5
                ,shade: 0.5
            });
        },
        openAudio: function (url) {
            var player;
            layer.open({
                type: 1,
                title: false,
                area: ['66%', '120px'],
                shade: 0.3,
                shadeClose: true,
                closeBtn: 1,
                anim: 5,
                content: '<div id="elementOfYourChoice"></div>',
                success: function(layero, index) {
                    $.get('/api/audioInfo', {url: url}, function (data) {
                        player = new Shikwasa({
                            fixed: {
                                type: 'auto',
                                position: 'bottom'
                            },
                            container: document.getElementById('elementOfYourChoice'),
                            transitionDuration: 5000,
                            themeColor: '#00869B',
                            autoPlay: false,
                            muted: false,
                            preload: 'metadata',
                            speedOptions: [0.5, 0.75, 1.25, 1.5],
                            audio: data.data
                        });
                    });
                },
                end: function () {
                    player.destroy();
                }
            });
        },
        removeDuplicateSeparator: function (path) {
            var result = '';
            for (var i = 0; i < path.length - 1; i++) {
                var current = path.charAt(i);
                var next = path.charAt(i + 1);
                if (!(current === '/' && next === '/')) {
                    result += current;
                }
            }
            result += path.charAt(path.length - 1);
            return result;
        },
        removePrefixPath: function (path) {
            if (path.indexOf(zfile.prefixPath) === 0) {
                return path.substr(zfile.prefixPath.length);
            }
            return path;
        }
    };

    zfile.listObjects(zfile.getPath());

    $("#search").on("click", function (e) {
        if ($(e.target).hasClass('l10n_ph-search')) {
            return;
        }
        $(this).toggleClass("active")
    });

    $("#crumbbar").on("click", 'a.crumb', function () {
        zfile.listObjects($(this).attr("data"));							// 回到首页, 看是不是可以直接写死为 '/'
    });

    $(".header").on("click", ".name, .time, .size", function () {     // 排序功能
        zfile.sortSwitch($(this));
    });

    // 返回上一页 (定义的 #back 是为了每次覆盖)
    $("#back").on("click", "li.item.folder.folder-parent", function (e) {
        e.preventDefault();
        var path = $(this).attr("data");
        var href = $(this).find("a").attr("href");
        zfile.listObjects(path);
        window.history.pushState(null, null, zfile.removeDuplicateSeparator(href));
    });

    // 当点击文件夹, 进入文件夹
    zfile.listObj.on("click", "li.item.folder", function (e) {
        e.preventDefault();
        var path = $(this).attr("data");
        zfile.listObjects(path);
        window.history.pushState(null, null, zfile.removeDuplicateSeparator(zfile.prefixPath + "/" + path));
    });

    zfile.listObj.on("click", "li.item.file", function (event) {
        $(this).attr('style', 'opacity: 0.5;-moz-opacit: 0.5;');

        var filePath = $(this).attr('data');
        var fileType = $(this).data('type');
        var fileName = $(this).find(".name").html();
        var url = $(this).find("a").attr('href');

        if (fileType === 'video') {
            event.preventDefault();
            zfile.openVideo(url);
            $(this).attr('style', 'opacity: 1.0;-moz-opacit: 1.0;');
            return;
        } else if (fileType === 'text' || fileType === 'markdown' || fileType === 'language') {
            event.preventDefault();
            zfile.openText(fileType, fileName, filePath);
            $(this).attr('style', 'opacity: 1.0;-moz-opacit: 1.0;');
            return;
        } else if (fileType === 'image') {
            event.preventDefault();
            zfile.openImage(url);
            $(this).attr('style', 'opacity: 1.0;-moz-opacit: 1.0;');
            return;
        } else if (fileType === 'audio') {
            event.preventDefault();
            zfile.openAudio(url);
            $(this).attr('style', 'opacity: 1.0;-moz-opacit: 1.0;');
            return;
        }
        $(this).attr('style', 'opacity: 1.0;-moz-opacit: 1.0;');
    });

    window.addEventListener("popstate", function(e) {
        zfile.listObjects(zfile.getPath());
    });

    return zfile;
});