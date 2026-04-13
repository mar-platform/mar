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
        title?: string;
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
    {#if title}
        <h2 class="text-xl font-medium">{title}</h2>
    {/if}
    <div class="rounded-md border shadow-sm bg-background overflow-x-auto">
        <Table.Root>
            <Table.Body>
                {#each table.getFlatHeaders() as header (header.id)}
                    <Table.Row class="hover:bg-muted/40 transition-colors">
                        <Table.Head class="bg-muted/50 font-semibold text-primary min-w-50 border-r align-middle h-[inherit] px-4">
                            {#if !header.isPlaceholder}
                                <FlexRender
                                    content={header.column.columnDef.header}
                                    context={header.getContext()}
                                />
                            {/if}
                        </Table.Head>
                        
                        {#each table.getRowModel().rows as row (row.id)}
                            {@const cell = row.getVisibleCells().find(c => c.column.id === header.column.id)}
                            {#if cell}
                                <Table.Cell class="py-3 px-4 min-w-37.5 whitespace-nowrap">
                                    <FlexRender
                                        content={cell.column.columnDef.cell}
                                        context={cell.getContext()}
                                    />
                                </Table.Cell>
                            {/if}
                        {:else}
                            <Table.Cell class="py-3 px-4 text-center text-muted-foreground w-full">
                                No results.
                            </Table.Cell>
                        {/each}
                    </Table.Row>
                {/each}
            </Table.Body>
        </Table.Root>
    </div>
</div>
