import type { Ref } from "vue";

/**
 * Pass through a selection of IDs from one ref `a` to another ref `b`.
 * Meant to be put in a watcher of ref `a`.
 * Modifies `b` so that it reacts to changes in `a`.
 *
 * When an element is removed from `a`, it's removed from `b`.
 * When an element is added to `a`, it's added to `b`.
 *
 * @param newVal in watcher, new value of `a`
 * @param oldVal in watcher, old value of `a`
 * @param model ref `b`
 */
export function passthroughSelection<T>(
  newVal: T[] | undefined,
  oldVal: T[] | undefined,
  model: Ref<T[] | undefined>,
) {
  if (!model.value) return;
  const _new = newVal ?? [];
  const _old = oldVal ?? [];
  const created = _new.filter((el) => !_old.includes(el) && !model.value!.includes(el));
  const removed = _old.filter((el) => !_new.includes(el) && model.value!.includes(el));

  if (removed.length) {
    model.value = model.value.filter((el) => removed.includes(el));
  }
  if (created.length) {
    model.value.push(...created);
  }
}
