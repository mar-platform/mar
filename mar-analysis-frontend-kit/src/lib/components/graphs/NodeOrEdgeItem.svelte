<script lang="ts">
    import { cn } from '$lib/utils';
import LucideCheck from '@lucide/svelte/icons/check';
	import { fade } from "svelte/transition";
    
    interface NodeOrEdgeItemProps {
        type: 'node' | 'edge';
        name: string;
        checked: boolean;
        color: string;
        onClick?: () => void;
        class?: string;
    }

    let {
        type,
        name,
        checked,
        color,
        onClick,
        class: className,
    }: NodeOrEdgeItemProps = $props();
</script>

<button 
    onclick={onClick}
    class={cn("select-none text-xs font-semibold flex items-center gap-2 px-1 py-0.5 transition-transform rounded-full", onClick ? "cursor-pointer active:scale-95 " : "", className)}
    style={`background-color: color-mix(in srgb, ${color}, transparent 85%); color: ${color}; border: 1px solid color-mix(in srgb, ${color}, transparent 60%);`}
>
    {#if type === 'node'}
        <div class="size-3 rounded-full" style={`background-color: ${color};`}></div>
    {:else}
        <svg class="size-2.5" style={`color: ${color};`} viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M0 16L16 0" stroke="currentColor" stroke-width="2.5" />
        </svg>
    {/if}
    {name}
    {#key checked}
        <div class="w-3" in:fade>
            {#if checked}
                <LucideCheck class="size-3" strokeWidth={3} color={color} />
            {/if}
        </div>    
    {/key}
</button>
