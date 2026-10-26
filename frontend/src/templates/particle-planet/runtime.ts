import { PP_ENGINE } from './engine'

/** 导出页面运行时脚本：注入共享粒子引擎，驱动的 DOM 结构与预览组件一致 */
export const RUNTIME_JS = `
(function(){
  var root = document.querySelector('.pp-root');
  if (root) (${PP_ENGINE})(root);
})();
`
