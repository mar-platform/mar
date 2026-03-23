<script lang="ts">
  import { cn } from "$lib/utils";
  import ChevronDown from "lucide-svelte/icons/chevron-down";

  let {
    class: className = "",
    header = "",
    active = false,
    children,
    ...restProps
  }: {
    class?: string;
    header?: string;
    active?: boolean;
    children?: any;
    [key: string]: any;
  } = $props();

  let open = $state(active);
</script>

<div class={cn("border-b", className)} {...restProps}>
  <button
    class="flex w-full items-center justify-between py-4 font-medium transition-all hover:underline cursor-pointer"
    onclick={() => (open = !open)}
  >
    {header}
    <ChevronDown
      class={cn("h-4 w-4 shrink-0 transition-transform duration-200", open && "rotate-180")}
    />
  </button>
  {#if open}
    <div class="pb-4 pt-0">
      {@render children?.()}
    </div>
  {/if}
</div>
