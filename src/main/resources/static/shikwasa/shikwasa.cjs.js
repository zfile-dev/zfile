'use strict';

var playerElement = "<div class=\"shk_cover\"> <div class=\"shk_img\"> </div> <button class=\"shk_btn\" aria-label=\"toggle play and pause\"> <svg class=\"shk_btn_play\" aria-hidden=\"true\" viewbox=\"0 0 82 82\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"> <g stroke-width=\"1\" fill=\"none\" fill-rule=\"evenodd\"> <circle stroke=\"#FFFFFF\" stroke-width=\"2\" fill-opacity=\"0.3\" fill=\"#FFFFFF\" cx=\"41\" cy=\"41\" r=\"40\"></circle> <path d=\"M27.7607105,56.949692 C26.7883519,56.949692 26,56.2390382 26,55.362513 L26,27.5871045 C26,26.7105793 26.7883519,26 27.7607105,26 L56.3720089,40.2843873 C56.3720089,40.2843873 57.6925418,41.4747715 56.3720089,42.6652302 C55.051476,43.85554 27.7607105,56.949692 27.7607105,56.949692 Z\" fill=\"#FBF9F9\"></path> </g> </svg> <svg class=\"shk_btn_pause\" aria-hidden=\"true\" viewbox=\"0 0 82 82\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"> <g fill=\"#FFFFFF\" stroke-width=\"1\" fill-rule=\"evenodd\" transform=\"translate(1.000000, 1.000000)\"> <rect id=\"shk_icon_rectangle\" x=\"28\" y=\"20\" width=\"6\" height=\"40\"></rect> <use xlink:href=\"#shk_icon_rectangle\" x=\"18\"></use> <circle stroke=\"#FFFFFF\" stroke-width=\"2\" fill-opacity=\"0.3\" fill-rule=\"nonzero\" cx=\"40\" cy=\"40\" r=\"40\"> </circle> </g> </svg> </button> </div> <div class=\"shk_main\"> <div class=\"shk_text\"> <div> <span class=\"shk_subtitle\"></span> </div> <div> <span class=\"shk_title\"></span> </div> </div> <div class=\"shk_controls\"> <a class=\"shk_btn shk_btn_download\" aria-label=\"download audio\" title=\"download audio\" target=\"_blank\" download> <svg aria-hidden=\"true\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\" viewbox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"> <path d=\"M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4\"></path> <polyline points=\"7 10 12 15 17 10\"></polyline> <line x1=\"12\" y1=\"15\" x2=\"12\" y2=\"3\"></line> </svg> </a> <button class=\"shk_btn shk_btn_backward\" aria-label=\"rewind 10 seconds\" title=\"rewind 10 seconds\"> <svg aria-hidden=\"true\" viewbox=\"0 0 22 24\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"> <g> <path d=\"M5.89478367e-05,12.9496403 L2.40630895,12.9496403 C2.40630895,17.7175662 6.25386188,21.5827338 11.0000589,21.5827338 C15.746256,21.5827338 19.5938089,17.7175662 19.5938089,12.9496403 C19.5938089,8.18171439 15.746256,4.31654676 11.0000589,4.31654676 L11,1.89928057 C17.0751912,1.89928057 22.0000589,6.84669514 22.0000589,12.9496403 C22.0000589,19.0525854 17.0751912,24 11.0000589,24 C4.9249267,24 5.89478336e-05,19.0525854 5.89478336e-05,12.9496403 Z\"></path> <g transform=\"translate(3.953125, 3.280576)\"> <path d=\"M3.19,4.81381295 L4.4,4.81381295 L4.4,14.676259 L2.79125,14.676259 L2.79125,6.76143885 C2.2,7.30014388 1.4575,7.70071942 0.55,7.96316547 L0.55,6.36086331 C0.99,6.25035971 1.4575,6.05697842 1.9525,5.78071942 C2.4475,5.47683453 2.86,5.15913669 3.19,4.81381295 Z\"></path> <path d=\"M9.96875,4.62043165 C11.11,4.62043165 12.00375,5.09007194 12.65,6.05697842 C13.255,6.96863309 13.5575,8.19798561 13.5575,9.74503597 C13.5575,11.2920863 13.255,12.5214388 12.65,13.4330935 C12.00375,14.3861871 11.11,14.8696403 9.96875,14.8696403 C8.81375,14.8696403 7.92,14.3861871 7.2875,13.4330935 C6.6825,12.5214388 6.38,11.2920863 6.38,9.74503597 C6.38,8.19798561 6.6825,6.96863309 7.2875,6.05697842 C7.92,5.09007194 8.81375,4.62043165 9.96875,4.62043165 Z M9.96875,5.98791367 C9.185,5.98791367 8.635,6.41611511 8.31875,7.30014388 C8.09875,7.89410072 7.98875,8.70906475 7.98875,9.74503597 C7.98875,10.7671942 8.09875,11.5821583 8.31875,12.1899281 C8.635,13.0601439 9.185,13.5021583 9.96875,13.5021583 C10.73875,13.5021583 11.28875,13.0601439 11.61875,12.1899281 C11.83875,11.5821583 11.94875,10.7671942 11.94875,9.74503597 C11.94875,8.70906475 11.83875,7.89410072 11.61875,7.30014388 C11.28875,6.41611511 10.73875,5.98791367 9.96875,5.98791367 Z\"></path> </g> <polygon transform=\"translate(6.875000, 3.280576) scale(-1, 1) rotate(90.000000) translate(-6.875000, -3.280576) \" points=\"6.875 -0.84442446 10.1555755 7.40557554 3.59442446 7.40557554\"></polygon> </g> </svg> </button> <button class=\"shk_btn shk_btn_forward\" aria-label=\"forward 10 seconds\" title=\"forward 10 seconds\"> <svg aria-hidden=\"true\" viewbox=\"0 0 22 24\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"> <g> <path d=\"M10.9999411,1.89928058 L10.9999411,4.31654676 C6.25377107,4.31657877 2.40625,8.18173413 2.40625,12.9496403 C2.40625,17.7175662 6.25380293,21.5827338 11,21.5827338 C15.7461971,21.5827338 19.59375,17.7175662 19.59375,12.9496403 L22,12.9496403 C22,19.0525854 17.0751322,24 11,24 C4.92486775,24 1.42108547e-14,19.0525854 1.42108547e-14,12.9496403 C1.42108547e-14,6.84671488 4.92483589,1.89931258 10.9999411,1.89928058 Z\"></path> <path d=\"M7.10875,8.11510791 L8.31875,8.11510791 L8.31875,17.977554 L6.71,17.977554 L6.71,10.0627338 C6.11875,10.6014388 5.37625,11.0020144 4.46875,11.2644604 L4.46875,9.66215827 C4.90875,9.55165468 5.37625,9.35827338 5.87125,9.08201439 C6.36625,8.7781295 6.77875,8.46043165 7.10875,8.11510791 Z\"></path> <path d=\"M13.90125,7.94244604 C15.0425,7.94244604 15.93625,8.41208633 16.5825,9.37899281 C17.1875,10.2906475 17.49,11.52 17.49,13.0670504 C17.49,14.6141007 17.1875,15.8434532 16.5825,16.7551079 C15.93625,17.7082014 15.0425,18.1916547 13.90125,18.1916547 C12.74625,18.1916547 11.8525,17.7082014 11.22,16.7551079 C10.615,15.8434532 10.3125,14.6141007 10.3125,13.0670504 C10.3125,11.52 10.615,10.2906475 11.22,9.37899281 C11.8525,8.41208633 12.74625,7.94244604 13.90125,7.94244604 Z M13.90125,9.30992806 C13.1175,9.30992806 12.5675,9.7381295 12.25125,10.6221583 C12.03125,11.2161151 11.92125,12.0310791 11.92125,13.0670504 C11.92125,14.0892086 12.03125,14.9041727 12.25125,15.5119424 C12.5675,16.3821583 13.1175,16.8241727 13.90125,16.8241727 C14.67125,16.8241727 15.22125,16.3821583 15.55125,15.5119424 C15.77125,14.9041727 15.88125,14.0892086 15.88125,13.0670504 C15.88125,12.0310791 15.77125,11.2161151 15.55125,10.6221583 C15.22125,9.7381295 14.67125,9.30992806 13.90125,9.30992806 Z\"></path> <polygon transform=\"translate(15.125000, 3.280576) rotate(90.000000) translate(-15.125000, -3.280576) \" points=\"15.125 -0.84442446 18.4055755 7.40557554 11.8444245 7.40557554\"></polygon> </g> </svg> </button> <button class=\"shk_btn shk_btn_speed\" aria-label=\"toggle playback rate\" title=\"change playback rate\" aria-live=\"polite\">1.0x</button> <button class=\"shk_btn shk_btn_volume\" aria-label=\"toggle volume\" title=\"toggle volume\"> <svg class=\"shk_btn_unmute\" aria-hidden=\"true\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\" viewbox=\"0 0 508.514 508.514\" style=\"enable-background:new 0 0 508.514 508.514;\" xml:space=\"preserve\"> <g> <path d=\"M271.483,0.109c-5.784-0.54-12.554,0.858-20.531,5.689c0,0-132.533,115.625-138.286,121.314\n          H39.725c-17.544,0.032-31.782,14.27-31.782,31.814v194.731c0,17.607,14.239,31.782,31.782,31.782h72.941\n          c5.753,5.753,138.286,117.277,138.286,117.277c7.977,4.799,14.747,6.229,20.531,5.689c11.76-1.112,20.023-10.965,22.534-21.358\n          c0.127-1.017,0.127-464.533-0.032-465.55C291.506,11.074,283.211,1.222,271.483,0.109z\"/> <path d=\"M342.962,309.798c-7.85,3.973-10.997,13.508-7.087,21.358c2.829,5.53,8.422,8.74,14.207,8.74\n          c2.384,0,4.799-0.572,7.151-1.684c32.132-16.209,52.091-48.341,52.091-83.938s-19.959-67.728-52.091-83.938\n          c-7.85-3.973-17.385-0.795-21.358,7.056c-3.909,7.85-0.763,17.385,7.087,21.358c21.326,10.743,34.579,32.005,34.579,55.524\n          S364.288,299.055,342.962,309.798z\"/> <path d=\"M339.72,59.32c-8.486-1.716-17.004,3.941-18.593,12.522c-1.716,8.645,3.909,17.004,12.522,18.688\n          c78.312,15.256,135.139,84.128,135.139,163.743S411.962,402.761,333.65,418.017c-8.613,1.684-14.239,10.011-12.554,18.656\n          c1.494,7.596,8.136,12.84,15.542,12.84l3.083-0.318c93.218-18.148,160.851-100.147,160.851-194.922S432.938,77.5,339.72,59.32z\"/> </g> </svg> <svg class=\"shk_btn_mute\" aria-hidden=\"true\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\" viewbox=\"0 0 508.528 508.528\" style=\"enable-background:new 0 0 508.528 508.528;\" xml:space=\"preserve\"> <g> <path d=\"M263.54,0.116c-5.784-0.54-12.554,0.858-20.531,5.689c0,0-132.533,115.625-138.317,121.314H31.782\n          C14.239,127.15,0,141.389,0,158.933v194.731c0,17.607,14.239,31.782,31.782,31.782h72.941\n          c5.784,5.753,138.317,117.277,138.317,117.277c7.977,4.799,14.747,6.229,20.531,5.689c11.76-1.112,20.023-10.965,22.534-21.358\n          c0.095-1.017,0.095-464.533-0.064-465.55C283.563,11.081,275.268,1.228,263.54,0.116z\"/> <path d=\"M447.974,254.28l54.857-54.857c7.596-7.564,7.596-19.864,0-27.428\n          c-7.564-7.564-19.864-7.564-27.428,0l-54.857,54.888l-54.888-54.888c-7.532-7.564-19.864-7.564-27.397,0\n          c-7.564,7.564-7.564,19.864,0,27.428l54.857,54.857l-54.857,54.888c-7.564,7.532-7.564,19.864,0,27.396\n          c7.532,7.564,19.864,7.564,27.396,0l54.888-54.857l54.857,54.857c7.564,7.564,19.864,7.564,27.428,0\n          c7.564-7.532,7.564-19.864,0-27.396L447.974,254.28z\"/> </g> </svg> </button> </div> <div class=\"shk_bar-wrap\"> <div class=\"shk_bar\" aria-label=\"progress bar\"> <div class=\"shk_bar_played\" aria-label=\"played progress\"> <span class=\"bar-handle\" aria-label=\"progress bar handle\"></span> </div> <div class=\"shk_bar_loaded\" aria-label=\"loaded progress\"></div> </div> </div> <div class=\"shk_bottom\"> <span class=\"shk_loader\" aria-live=\"polite\"> <span class=\"shk_visuallyhidden\" tabindex=\"-1\">loading</span> <svg aria-hidden=\"true\" width=\"174px\" height=\"174px\" viewbox=\"0 0 66 66\" xmlns=\"http://www.w3.org/2000/svg\" aria-label=\"loading\" aria-live=\"polite\"> <circle fill=\"transparent\" stroke-width=\"6\" stroke-dasharray=\"170\" stroke-dashoffset=\"20\" cx=\"33\" cy=\"33\" r=\"30\" stroke=\"url(#shk_gradient)\"> </circle> <lineargradient id=\"shk_gradient\"> <stop offset=\"50%\" stop-color=\"currentColor\" stop-opacity=\"1\"/> <stop offset=\"65%\" stop-color=\"currentColor\" stop-opacity=\".5\"/> <stop offset=\"100%\" stop-color=\"currentColor\" stop-opacity=\"0\"/> </lineargradient> </svg> </span> <span class=\"shk_time\"> <span class=\"shk_time_now\">--:--</span> <span class=\"shk_time_duration\">--:--</span> </span> </div> </div> ";

const CONFIG = {
  container: document.querySelector('body'),
  fixed: {
    type: 'auto',
    position: 'bottom',
  },
  download: true,
  transitionDuration: 5000,
  themeColor: '#00869B',
  autoPlay: false,
  muted: false,
  preload: 'metadata',
  speedOptions: [0.5, 0.75, 1.25, 1.5],
  audio: null,
};

function secondToTime(time) {
  time = Math.round(time);
  let hour = Math.floor(time / 3600);
  let min = Math.floor((time - hour * 3600) / 60);
  let sec = Math.floor(time - hour * 3600 - min * 60);
  min = min < 10 ? '0' + min : min;
  sec = sec < 10 ? '0' + sec : sec;
  if (hour === 0) {
    hour = hour < 10 ? '0' + hour : hour;
    return `${min}:${sec}`
  }
  return `${hour}:${min}:${sec}`
}

function numToString(num) {
  const float = parseFloat(num).toFixed(2);
  return float.slice(-1) === '0' ? float.slice(0, -1) :float
}

function carousel(el, distance = 0, duration = 5000, pause = 2000) {
  const interval = duration + pause;
  function transform() {
    el.style.transitionDuration = `${duration / 1000}s`;
    el.style.transform = `translateX(${distance}px)`;
    setTimeout(() => {
      el.style.transform = 'translateX(0px)';
    }, interval);
  }
  transform();
  return setInterval(() => transform(), interval * 2)
}

function handleOptions(options) {
  options.container = options.container || CONFIG.container;
  options.fixed = options.fixed || CONFIG.fixed;
  options.download = typeof options.download === 'boolean' ? options.download : CONFIG.download;
  const fixedOptions = ['auto', 'static', 'fixed'];
  const result = fixedOptions.filter(item => item === options.fixed.type)[0];
  if (!result) {
    options.fixed.type = CONFIG.fixed.type;
  }
  options.transitionDuration = +options.transitionDuration || CONFIG.transitionDuration;
  options.themeColor = options.themeColor || CONFIG.themeColor;
  options.autoPlay = options.autoPlay || CONFIG.autoPlay;
  options.muted = options.muted || CONFIG.muted;
  options.preload = options.preload || CONFIG.preload;
  options.speedOptions = options.speedOptions || CONFIG.speedOptions;
  if (!Array.isArray(options.speedOptions)) {
    options.speedOptions = [options.speedOptions];
  }
  if (!options.speedOptions.includes(1)) {
    options.speedOptions.push(1);
  }
  options.speedOptions = options.speedOptions
  .map(sp => parseFloat(sp))
  .filter(sp => !isNaN(sp));
  if (options.speedOptions.length > 1) {
    options.speedOptions.sort((a, b) => a - b);
  }
  if (!options.audio) {
    console.error('audio is not specified');
  } else {
    options.audio.title = options.audio.title || 'Unknown Title';
    options.audio.artist = options.audio.artist || 'Unknown Artist';
    options.audio.cover = options.audio.cover || null;
    options.audio.duration = options.audio.duration || 0;
  }
  return options
}

class Template {
  constructor(container, options) {
    if (!container) return
    this.playBtn = container.querySelector('.shk_cover .shk_btn');
    this.downloadBtn = container.querySelector('.shk_btn_download');
    this.fwdBtn = container.querySelector('.shk_btn_forward');
    this.bwdBtn = container.querySelector('.shk_btn_backward');
    this.speedBtn = container.querySelector('.shk_btn_speed');
    this.muteBtn = container.querySelector('.shk_btn_volume');
    this.artist = container.querySelector('.shk_subtitle');
    this.texts = container.querySelector('.shk_text');
    this.title = container.querySelector('.shk_title');
    this.subtitle = container.querySelector('.shk_subtitle');
    this.currentTime = container.querySelector('.shk_time_now');
    this.duration = container.querySelector('.shk_time_duration');
    this.bar = container.querySelector('.shk_bar');
    this.barWrap = container.querySelector('.shk_bar-wrap');
    this.audioPlayed = container.querySelector('.shk_bar_played');
    this.audioLoaded = container.querySelector('.shk_bar_loaded');
    this.handle = container.querySelector('.bar-handle');
    this.cover = container.querySelector('.shk_img');

    this.audioPlayed.style.color = options.themeColor + '70';
      if (options.download && options.audio && options.audio.src) {
      this.downloadBtn.href= options.audio.src;
    } else {
      this.downloadBtn.remove();
    }
    if (options.audio) {
      this.update(options.audio);
    }
  }

  update(audio) {
    this.cover.style.backgroundImage = `url(${audio.cover})`;
    this.title.innerHTML = audio.title;
    this.artist.innerHTML = audio.artist;
    this.currentTime.innerHTML = '00:00';
    this.duration.innerHTML = audio.duration ? secondToTime(audio.duration) : '00:00';
  }
}

class Bar {
  constructor(template) {
    this.elements = {};
    this.elements.audioPlayed = template.audioPlayed;
    this.elements.audioLoaded = template.audioLoaded;
  }

  set (type, percentage) {
    percentage = Math.min(percentage, 1);
    percentage = Math.max(percentage, 0);
    this.elements[type].style.width = percentage * 100 + '%';
  }
}

let carouselInterval, pressSpace;
const isMobile = /mobile/i.test(window.navigator.userAgent);
const dragStart = isMobile ? 'touchstart' : 'mousedown';
const dragMove = isMobile ? 'touchmove' : 'mousemove';
const dragEnd = isMobile ? 'touchend' : 'mouseup';

class Player {
  constructor(options) {
    this.el = document.createElement('div');
    this.el.classList.add('shk');
    this.options = handleOptions(options);
    this.inited = false;
    this.muted = this.options.muted;
    this.initUI();
    this.initKeyEvents();
    this.dragging = false;
    this.currentSpeed = 1;
    this.currentTime = 0;
    this.initAudio();
    this.mount(this.options.container);
    this.afterMount();
  }

  get duration() {
    if (!this.audio) {
      return this.options.audio.duration
    } else {
      return isNaN(this.audio.duration) ? 0 : this.audio.duration
    }
  }

  initUI() {
    this.el.innerHTML = playerElement;
    this.el.style = `--theme-color: ${this.options.themeColor}`;
    this.el.style.boxShadow = `0px 0px 14px 6px ${this.options.themeColor}20`;
    this.template = new Template(this.el, this.options);
    this.bar = new Bar(this.template);
    this.initOptions();
    this.initButtons();
    this.initBar();
  }

  initOptions() {
    if (this.options.fixed.type !== 'static' ) {
      this.options.fixed.type === 'fixed' ? this.el.classList.add('Fixed') : this.el.classList.add('Auto');
      if (this.options.fixed.position === 'top') {
        this.el.classList.add('Top');
      }
    }
    if (this.options.muted) {
      this.el.classList.add('Mute');
    }
    this.options.autoPlay ? this.el.classList.add('Play') : this.el.classList.add('Pause');
  }

  initButtons() {
    this.template.playBtn.addEventListener('click', () => {
      this.toggle();
    });
    this.template.muteBtn.addEventListener('click', () => {
      this.muted = !this.muted;
      this.el.classList.toggle('Mute');
      if (this.audio) {
        this.audio.muted = this.muted;
      }
    });
    this.template.fwdBtn.addEventListener('click', () => {
      const time = Math.min(this.duration, this.currentTime + 10);
      this.seek(time);
    });
    this.template.bwdBtn.addEventListener('click', () => {
      const time = Math.max(0, this.currentTime - 10);
      this.seek(time);
    });
    this.template.speedBtn.addEventListener('click', () => {
      const index = this.options.speedOptions.indexOf(this.currentSpeed);
      const speedRange = this.options.speedOptions;
      this.currentSpeed = (index + 1 >= speedRange.length) ? speedRange[0] : speedRange[index + 1];
      this.template.speedBtn.innerHTML = numToString(this.currentSpeed) + 'x';
      if (this.audio) {
        this.audio.playbackRate = this.currentSpeed;
      }
    });
  }

  initBar() {
    const dragStartHandler = () => {
      this.el.classList.add('Seeking');
      this.dragging = true;
      document.addEventListener(dragMove, dragMoveHandler);
      document.addEventListener(dragEnd, dragEndHandler);
    };

    const dragMoveHandler = (e) => {
      let percentage = ((e.clientX || e.changedTouches[0].clientX) - this.template.barWrap.getBoundingClientRect().left) / this.template.barWrap.clientWidth;
      percentage = Math.min(percentage, 1);
      percentage = Math.max(0, percentage);
      this.bar.set('audioPlayed', percentage);
      this.currentTime = percentage * this.duration;
      this.template.currentTime.innerHTML = secondToTime(this.currentTime);
    };

    const dragEndHandler = (e) => {
      this.dragging = false;
      this.el.classList.remove('Seeking');
      this.seek(this.currentTime);
      document.removeEventListener(dragMove, dragMoveHandler);
      document.removeEventListener(dragEnd, dragEndHandler);
    };

    const instantSeek = (e) => {
      if (this.dragging) return
      dragMoveHandler(e);
      this.seek(this.currentTime);
    };
    this.template.barWrap.addEventListener(dragEnd, instantSeek);
    this.template.handle.addEventListener(dragStart, dragStartHandler);
  }

  initKeyEvents() {
    pressSpace = (e) => {
      if (e.keyCode === 32) {
        this.toggle();
      }
    };
    document.addEventListener('keyup', pressSpace);
  }

  initAudio() {
    if (this.options.audio.src) {
      this.audio = new Audio();
      this.initLoadingEvents();
      this.initAudioEvents();
      if (this.options.preload !== 'none') {
        this.updateAudio(this.options.audio.src);
        this.inited = true;
      }
    }
  }

  initAudioEvents() {
    this.audio.addEventListener('play', () => {
      if (this.el.classList.contains('Pause')) {
        this.setUIPlaying();
      }
    });
    this.audio.addEventListener('pause', () => {
      if (this.el.classList.contains('Pause')) {
        this.setUIPaused();
      }
    });
    this.audio.addEventListener('ended', () => {
      this.setUIPaused();
      this.seek(0);
    });
    this.audio.addEventListener('durationchange', () => {
      if (this.duration !== 1) {
        this.template.duration.innerHTML = secondToTime(this.duration);
      }
    });
    this.audio.addEventListener('progress', () => {
      if (this.audio.buffered.length) {
        const percentage = this.audio.buffered.length ? this.audio.buffered.end(this.audio.buffered.length - 1) / this.duration : 0;
        this.bar.set('audioLoaded', percentage);
      }
    });
    this.audio.addEventListener('timeupdate', () => {
      if (this.dragging) return
      if (Math.floor(this.currentTime) !== Math.floor(this.audio.currentTime)) {
        this.template.currentTime.innerHTML = secondToTime(this.audio.currentTime);
        this.currentTime = +this.audio.currentTime;
        const percentage = this.audio.currentTime ? this.audio.currentTime / this.duration : 0;
        this.bar.set('audioPlayed', percentage);
      }
    });
  }

  initLoadingEvents() {
    const addLoadingClass = () => {
      if (this.el.classList.contains('Loading')) {
        this.el.classList.remove('Loading');
      }
    };
    this.audio.addEventListener('canplay', () => {
      addLoadingClass();
    });
    this.audio.addEventListener('canplaythrough', () => {
      addLoadingClass();
    });
    this.audio.addEventListener('waiting', () => {
      if (!this.el.classList.contains('Loading')) {
        this.el.classList.add('Loading');
      }
    });
  }

  setUIPlaying() {
    this.el.classList.add('Play');
    this.el.classList.remove('Pause');
  }

  setUIPaused() {
    this.el.classList.add('Pause');
    this.el.classList.remove('Play');
    this.el.classList.remove('Loading');
  }

  play(audio) {
    if (!this.inited) {
      this.audio.src = this.options.audio.src;
      this.inited = true;
    }
    if (audio && audio.src) {
      this.template.update(audio);
      this.currentTime = 0;
      this.updateAudio(audio.src);
    }
    if (!this.audio.paused) return
    this.setUIPlaying();
    const promise = this.audio.play();
    if (promise instanceof Promise) {
      promise.catch((e) => {
        if (e.name === 'NotAllowedError' || e.name === 'NotSupportedError') {
          this.pause();
        }
      });
    }
  }

  pause() {
    if (this.audio.paused) return
    this.setUIPaused();
    this.audio.pause();
  }

  toggle() {
    if (!this.inited) {
      this.audio.src = this.options.audio.src;
      this.inited = true;
    }

    this.audio.paused ? this.play() : this.pause();
  }

  seek(time) {
    time = Math.min(time, this.duration);
    time = Math.max(time, 0);
    this.template.currentTime.innerHTML = secondToTime(time);
    if (this.audio) {
      this.audio.currentTime = time;
    } else {
      this.currentTime = time;
    }
  }

  updateAudio(src) {
    this.audio.src = src;
    this.audio.autoplay = false;
    this.audio.preload = this.options.preload;
    this.audio.autoplay = this.options.autoPlay;
    this.audio.muted = this.muted;
    this.audio.currentTime = this.currentTime;
    this.audio.playbackRate = this.currentSpeed;
  }

  mount(container) {
    container.append(this.el);
  }

  afterMount() {
    const titleOverflow = this.template.title.offsetWidth - this.template.texts.offsetWidth;
    if (titleOverflow > 0) {
      carouselInterval = carousel(this.template.title, -titleOverflow, this.options.transitionDuration);
    }
  }

  destroy() {
    this.audio.pause();
    this.audio.src = '';
    this.audio.load();
    this.audio = null;
    clearInterval(carouselInterval);
    document.removeEventListener('keyup', pressSpace);
  }
}

module.exports = Player;
