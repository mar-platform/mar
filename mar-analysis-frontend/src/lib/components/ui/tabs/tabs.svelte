<script lang="ts">
  import { cn } from "$lib/utils";

  let {
    class: className = "",
    value = $bindable(""),
    children,
    ...restProps
  }: {
    class?: string;
    value?: string;
    children?: any;
    [key: string]: any;
  } = $props();
</script>

<div class={cn("w-full", className)} {...restProps}>
  {@render children?.()}
</div>
