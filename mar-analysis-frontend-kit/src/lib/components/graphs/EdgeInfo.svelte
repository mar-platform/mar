<script lang="ts">
  import type Graph from 'graphology'

  let { edge, graph, onNodeSelect }: {
    edge: { key: string; type: string; sourceId: string; targetId: string };
    graph: Graph;
    onNodeSelect?: (node: any) => void;
  } = $props();

  let sourceAttrs = $derived(graph.getNodeAttributes(edge.sourceId));
  let targetAttrs = $derived(graph.getNodeAttributes(edge.targetId));
</script>

<div class="text-sm flex flex-col gap-3">
  <div>
    <p class="font-medium mb-1">Relationship</p>
    <dl class="space-y-0.5">
      <div class="flex gap-2">
        <dt class="text-muted-foreground shrink-0">Type:</dt>
        <dd>{edge.type}</dd>
      </div>
    </dl>
  </div>

  <div>
    <p class="font-medium mb-1">Source</p>
    <button
      class="text-left hover:underline cursor-pointer break-all"
      onclick={() => onNodeSelect?.(sourceAttrs.impl)}
    >{sourceAttrs.label}</button>
  </div>

  <div>
    <p class="font-medium mb-1">Target</p>
    <button
      class="text-left hover:underline cursor-pointer break-all"
      onclick={() => onNodeSelect?.(targetAttrs.impl)}
    >{targetAttrs.label}</button>
  </div>
</div>
