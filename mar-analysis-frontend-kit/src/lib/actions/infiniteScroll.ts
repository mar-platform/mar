import type { Action } from 'svelte/action';

export const infiniteScroll: Action<HTMLElement, () => void> = (node, onIntersect) => {
  const observer = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting) {
        onIntersect();
      }
    },
    { 
      root: null,
      rootMargin: '0px', 
      threshold: 0.1
    }
  );

  observer.observe(node);

  return {
    update(newCallback) {
      onIntersect = newCallback;
    },
    destroy() {
      observer.disconnect();
    }
  };
};
