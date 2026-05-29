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

<div
  class={cn(
    "mt-2 ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2",
    className
  )}
  role="tabpanel"
  {...restProps}
>
  {@render children?.()}
</div>
