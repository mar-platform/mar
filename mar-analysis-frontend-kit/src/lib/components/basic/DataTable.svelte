<script lang="ts" generics="TData, TValue">
	import { type ColumnDef, getCoreRowModel } from "@tanstack/table-core";
    import {
        createSvelteTable,
        FlexRender,
    } from "$lib/components/ui/data-table/index.js";
    import * as Table from "$lib/components/ui/table/index.js";
	import { cn } from "$lib/utils";

    type DataTableProps<TData, TValue> = {
        columns: ColumnDef<TData, TValue>[];
        data: TData[];
        class?: string;
        title: string;
    };

    let { data, columns, class: className, title }: DataTableProps<TData, TValue> = $props();

    const table = $derived(createSvelteTable({
        get data() {
            return data;
        },
        columns,
        getCoreRowModel: getCoreRowModel(),
    }));
</script>

<div class={cn("flex flex-col gap-1.5", className)}>
  <h2 class="text-xl font-medium">{title}</h2>
  <div class="rounded-md border shadow-sm bg-background overflow-hidden">
    <Table.Root>
      <Table.Header class="bg-muted/50">
        {#each table.getHeaderGroups() as headerGroup (headerGroup.id)}
          <Table.Row class="hover:bg-transparent">
            {#each headerGroup.headers as header (header.id)}
              <Table.Head colspan={header.colSpan} class="font-semibold text-primary h-11">
                {#if !header.isPlaceholder}
                  <FlexRender
                    content={header.column.columnDef.header}
                    context={header.getContext()}
                  />
                {/if}
              </Table.Head>
            {/each}
          </Table.Row>
        {/each}
      </Table.Header>
      <Table.Body>
        {#each table.getRowModel().rows as row (row.id)}
          <Table.Row data-state={row.getIsSelected() && "selected"} class="hover:bg-muted/40 transition-colors">
            {#each row.getVisibleCells() as cell (cell.id)}
              <Table.Cell class="py-3">
                <FlexRender
                  content={cell.column.columnDef.cell}
                  context={cell.getContext()}
                />
              </Table.Cell>
            {/each}
          </Table.Row>
        {:else}
          <Table.Row>
            <Table.Cell colspan={columns.length} class="h-24 text-center text-muted-foreground">
              No empty results.
            </Table.Cell>
          </Table.Row>
        {/each}
      </Table.Body>
    </Table.Root>
  </div>
</div>

