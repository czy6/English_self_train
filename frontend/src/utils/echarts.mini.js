// ECharts轻量版 - 仅包含雷达图功能
(function (global, factory) {
  typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports) :
  typeof define === 'function' && define.amd ? define(['exports'], factory) :
  (global = typeof globalThis !== 'undefined' ? globalThis : global || self, factory(global.echarts = {}));
}(this, (function (exports) {
  'use strict';

  // 基础工具函数
  const util = {
    extend: function (target, source) {
      for (let key in source) {
        if (source.hasOwnProperty(key)) {
          target[key] = source[key];
        }
      }
      return target;
    },
    
    isArray: function (obj) {
      return Object.prototype.toString.call(obj) === '[object Array]';
    },
    
    isObject: function (obj) {
      return obj !== null && typeof obj === 'object';
    }
  };

  // 颜色工具
  const color = {
    parse: function (colorStr) {
      if (colorStr.indexOf('rgba') === 0) {
        const match = colorStr.match(/rgba\((\d+),\s*(\d+),\s*(\d+),\s*([\d.]+)\)/);
        if (match) {
          return {
            r: parseInt(match[1]),
            g: parseInt(match[2]),
            b: parseInt(match[3]),
            a: parseFloat(match[4])
          };
        }
      }
      return { r: 0, g: 0, b: 0, a: 1 };
    }
  };

  // 雷达图核心类
  class RadarChart {
    constructor(dom, opts) {
      this.dom = dom;
      this.opts = opts || {};
      this.width = opts.width || dom.clientWidth;
      this.height = opts.height || dom.clientHeight;
      this.ctx = dom.getContext('2d');
      this.init();
    }

    init() {
      this.ctx.scale(1, 1);
    }

    setOption(option) {
      this.option = option;
      this.render();
    }

    render() {
      this.clear();
      this.drawRadar();
      this.drawData();
    }

    clear() {
      this.ctx.clearRect(0, 0, this.width, this.height);
    }

    drawRadar() {
      const radarOpt = this.option.radar;
      if (!radarOpt) return;

      const centerX = this.width / 2;
      const centerY = this.height / 2;
      const radius = Math.min(centerX, centerY) * 0.7;
      const indicatorCount = radarOpt.indicator.length;
      const angleStep = (Math.PI * 2) / indicatorCount;

      // 绘制雷达网格
      this.drawRadarGrid(centerX, centerY, radius, indicatorCount, angleStep, radarOpt);
      
      // 绘制指示器标签
      this.drawIndicatorLabels(centerX, centerY, radius, indicatorCount, angleStep, radarOpt);
    }

    drawRadarGrid(centerX, centerY, radius, indicatorCount, angleStep, radarOpt) {
      const splitNumber = radarOpt.splitNumber || 4;
      
      // 绘制分割区域
      for (let i = splitNumber; i > 0; i--) {
        const currentRadius = (radius * i) / splitNumber;
        
        this.ctx.beginPath();
        for (let j = 0; j <= indicatorCount; j++) {
          const angle = j * angleStep - Math.PI / 2;
          const x = centerX + currentRadius * Math.cos(angle);
          const y = centerY + currentRadius * Math.sin(angle);
          
          if (j === 0) {
            this.ctx.moveTo(x, y);
          } else {
            this.ctx.lineTo(x, y);
          }
        }
        
        // 设置样式
        if (radarOpt.splitArea && radarOpt.splitArea.show) {
          this.ctx.fillStyle = i % 2 === 0 ? 
            (radarOpt.splitArea.areaStyle && radarOpt.splitArea.areaStyle.color ? 
             radarOpt.splitArea.areaStyle.color[0] : 'rgba(255, 255, 255, 0.5)') :
            (radarOpt.splitArea.areaStyle && radarOpt.splitArea.areaStyle.color ? 
             radarOpt.splitArea.areaStyle.color[1] : 'rgba(200, 200, 200, 0.1)');
          this.ctx.fill();
        }
        
        // 绘制分割线
        if (radarOpt.splitLine && radarOpt.splitLine.show) {
          this.ctx.strokeStyle = radarOpt.splitLine.lineStyle ? 
            radarOpt.splitLine.lineStyle.color : 'rgba(211, 211, 211, 0.5)';
          this.ctx.lineWidth = radarOpt.splitLine.lineStyle ? 
            radarOpt.splitLine.lineStyle.width || 1 : 1;
          this.ctx.stroke();
        }
      }

      // 绘制轴线
      if (radarOpt.axisLine && radarOpt.axisLine.show) {
        this.ctx.strokeStyle = radarOpt.axisLine.lineStyle ? 
          radarOpt.axisLine.lineStyle.color : 'rgba(211, 211, 211, 0.8)';
        this.ctx.lineWidth = radarOpt.axisLine.lineStyle ? 
          radarOpt.axisLine.lineStyle.width || 1 : 1;
        
        for (let i = 0; i < indicatorCount; i++) {
          const angle = i * angleStep - Math.PI / 2;
          const x = centerX + radius * Math.cos(angle);
          const y = centerY + radius * Math.sin(angle);
          
          this.ctx.beginPath();
          this.ctx.moveTo(centerX, centerY);
          this.ctx.lineTo(x, y);
          this.ctx.stroke();
        }
      }
    }

    drawIndicatorLabels(centerX, centerY, radius, indicatorCount, angleStep, radarOpt) {
      if (!radarOpt.axisName || !radarOpt.axisName.show) return;

      this.ctx.fillStyle = radarOpt.axisName.color || '#666';
      this.ctx.font = `${radarOpt.axisName.fontSize || 12}px sans-serif`;
      this.ctx.textAlign = 'center';
      this.ctx.textBaseline = 'middle';

      for (let i = 0; i < indicatorCount; i++) {
        const angle = i * angleStep - Math.PI / 2;
        const labelRadius = radius * 1.1;
        const x = centerX + labelRadius * Math.cos(angle);
        const y = centerY + labelRadius * Math.sin(angle);
        
        this.ctx.fillText(radarOpt.indicator[i].name, x, y);
      }
    }

    drawData() {
      const series = this.option.series;
      if (!series || !util.isArray(series)) return;

      series.forEach(serie => {
        if (serie.type === 'radar') {
          this.drawRadarData(serie);
        }
      });
    }

    drawRadarData(serie) {
      const radarOpt = this.option.radar;
      if (!radarOpt || !serie.data || !util.isArray(serie.data)) return;

      const centerX = this.width / 2;
      const centerY = this.height / 2;
      const radius = Math.min(centerX, centerY) * 0.7;
      const indicatorCount = radarOpt.indicator.length;
      const angleStep = (Math.PI * 2) / indicatorCount;

      serie.data.forEach(dataItem => {
        if (!dataItem.value || !util.isArray(dataItem.value)) return;

        // 绘制数据区域
        this.ctx.beginPath();
        for (let i = 0; i <= indicatorCount; i++) {
          const indicatorIndex = i % indicatorCount;
          const maxValue = radarOpt.indicator[indicatorIndex].max || 100;
          const value = dataItem.value[indicatorIndex] || 0;
          const valueRadius = (value / maxValue) * radius;
          const angle = i * angleStep - Math.PI / 2;
          
          const x = centerX + valueRadius * Math.cos(angle);
          const y = centerY + valueRadius * Math.sin(angle);
          
          if (i === 0) {
            this.ctx.moveTo(x, y);
          } else {
            this.ctx.lineTo(x, y);
          }
        }

        // 填充区域
        if (dataItem.areaStyle && dataItem.areaStyle.show !== false) {
          this.ctx.fillStyle = dataItem.areaStyle.color || 'rgba(103, 114, 229, 0.3)';
          this.ctx.fill();
        }

        // 绘制线条
        this.ctx.strokeStyle = dataItem.lineStyle ? 
          dataItem.lineStyle.color || '#6772e5' : '#6772e5';
        this.ctx.lineWidth = dataItem.lineStyle ? 
          dataItem.lineStyle.width || 2 : 2;
        this.ctx.stroke();

        // 绘制数据点
        for (let i = 0; i < indicatorCount; i++) {
          const maxValue = radarOpt.indicator[i].max || 100;
          const value = dataItem.value[i] || 0;
          const valueRadius = (value / maxValue) * radius;
          const angle = i * angleStep - Math.PI / 2;
          
          const x = centerX + valueRadius * Math.cos(angle);
          const y = centerY + valueRadius * Math.sin(angle);
          
          this.ctx.beginPath();
          this.ctx.arc(x, y, 3, 0, Math.PI * 2);
          this.ctx.fillStyle = dataItem.itemStyle ? 
            dataItem.itemStyle.color || '#6772e5' : '#6772e5';
          this.ctx.fill();
        }
      });
    }

    resize(width, height) {
      this.width = width || this.dom.clientWidth;
      this.height = height || this.dom.clientHeight;
      this.render();
    }

    dispose() {
      this.ctx = null;
      this.dom = null;
    }
  }

  // ECharts主类
  class ECharts {
    constructor(dom, theme, opts) {
      if (typeof dom === 'string') {
        dom = document.getElementById(dom);
      }
      
      if (!dom) {
        throw new Error('Invalid dom element');
      }

      this.dom = dom;
      this.theme = theme;
      this.opts = opts || {};
      this.charts = {};
    }

    setOption(option, notMerge, lazyUpdate) {
      if (!this.charts.radar) {
        this.charts.radar = new RadarChart(this.dom, this.opts);
      }
      this.charts.radar.setOption(option);
    }

    resize() {
      if (this.charts.radar) {
        this.charts.radar.resize();
      }
    }

    dispose() {
      if (this.charts.radar) {
        this.charts.radar.dispose();
      }
      this.charts = {};
    }
  }

  // 全局初始化函数
  function init(dom, theme, opts) {
    return new ECharts(dom, theme, opts);
  }

  // 导出API
  exports.init = init;
  exports.version = '5.4.3-mini';

  Object.defineProperty(exports, '__esModule', { value: true });
})));