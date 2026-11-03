import { CUBE_ENGINE } from './engine'
import { MEDIA_CAROUSEL_JS } from '../mediaCarousel'

/** 导出页面运行时脚本：注入共享交互引擎，驱动的 DOM 结构与预览组件一致 */
export const RUNTIME_JS = `
(function(){
  var root = document.querySelector('.cd-root');
  if (root) (${CUBE_ENGINE})(root);

  // 作品卡片媒体轮播(视频首帧 + 图集,箭头切换 + 图片帧自动轮播)
  ${MEDIA_CAROUSEL_JS}
})();
`
