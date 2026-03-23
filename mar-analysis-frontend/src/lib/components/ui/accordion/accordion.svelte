<script lang="ts">
  import { cn } from "$lib/utils";

  let {
    class: className = "",
    children,
    ...restProps
  }: {
    class?: string;
    children?: any;
    [key: string]: any;
  } = $props();
</script>

<div class={cn("w-full", className)} {...restProps}>
  {@render children?.()}
</div>
