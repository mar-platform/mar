<script lang="ts">
    /* eslint-disable svelte/no-navigation-without-resolve */
	import * as NavigationMenu from '$lib/components/ui/navigation-menu/index.js';
	import { navigationMenuTriggerStyle } from '$lib/components/ui/navigation-menu/navigation-menu-trigger.svelte';
	import { COMPONENT_GRAPHS_EXPLORATION_PATH, GITHUB_PATH, INTERPROJECT_GRAPHS_EXPLORATION_PATH, MEGAMODEL_GRAPHS_EXPLORATION_PATH, PROJECT_GRAPHS_EXPLORATION_PATH, STATS_PATH } from '$lib/constants/routes';
    import Logo from '$lib/assets/logo.png';
	import { Button } from "$lib/components/ui/button/index.js";
    import * as Breadcrumb from "$lib/components/ui/breadcrumb/index.js";

    import LucideMenu from '@lucide/svelte/icons/menu';
    import LucideX from '@lucide/svelte/icons/x';
    import LucideSun from '@lucide/svelte/icons/sun';
    import LucideMoon from '@lucide/svelte/icons/moon';
    import GithubWhiteLogo from '$lib/assets/github-white-icon.svg';
    import GithubLogo from '$lib/assets/github-icon.svg';
	import { mode, toggleMode } from 'mode-watcher';
	import { cn } from '$lib/utils';
	import { page } from '$app/state';
	import ChevronRight from '@lucide/svelte/icons/chevron-right';
	import { APP_NAME, SHOW_LOGOS_AND_REPO } from '$lib/constants/values';

    type RouteItem = {
        title: string;
        href: string | null;
        subList?: Omit<RouteItem, 'subList'>[];
    }

    const showAsNestedMenus = false;
    let isVerticalMenuOpen = $state(false);

    const routes: RouteItem[] = [
        { title: "Stats", href: STATS_PATH },
        { 
            title: "Graphs",
            href: null,
            subList: [
                { title: "Megamodel", href: MEGAMODEL_GRAPHS_EXPLORATION_PATH },
                { title: "Inter-project", href: INTERPROJECT_GRAPHS_EXPLORATION_PATH },
                { title: "Projects", href: PROJECT_GRAPHS_EXPLORATION_PATH },
                { title: "Components", href: COMPONENT_GRAPHS_EXPLORATION_PATH },
            ],
        },
    ] as const;

    let breadcrumbs = $derived.by(() => {
        const path = page.url.href;
        const crumbs: { title: string; href: string | null }[] = [];
        
        for (const route of routes) {
            if (route.subList) {
                const subMatch = route.subList.find(s => path === s.href || path.startsWith(s.href + '/'));
                if (subMatch) {
                    crumbs.push({ title: route.title, href: route.href });
                    crumbs.push({ title: subMatch.title, href: subMatch.href });
                    return crumbs;
                }
            }
            
            const resolvedHref = route.href;
            if (path === resolvedHref || (resolvedHref !== '/' && path.startsWith(resolvedHref + '/'))) {
                crumbs.push({ title: route.title, href: route.href });
                return crumbs;
            }
        }
        
        return crumbs;
    });
</script>

<header>
    <div class="absolute z-10 top-0 left-0 right-0 flex flex-col items-center m-5 p-5 bg-page-foreground rounded-xl shadow-sm">
        <div class="flex gap-10 items-center justify-between w-full">
            <a href={STATS_PATH} class="flex items-center gap-3">
                {#if SHOW_LOGOS_AND_REPO}
                    <img src={Logo} alt="Logo" class="h-10 w-auto" />
                {/if}
                <span class="font-semibold">{APP_NAME}</span>
            </a>
            <NavigationMenu.Root viewport={false} class="min-[1150px]:block hidden">
                <NavigationMenu.List class="flex-wrap w-full">
                    {#each routes as route(route.title)}
                        <NavigationMenu.Item class="hidden md:block">
                            {#if route.subList}
                                {#if showAsNestedMenus}
                                    <NavigationMenu.Trigger class={cn("dark:bg-page-foreground", navigationMenuTriggerStyle(), page.url.href === route.href ? "font-bold" : "")}>{route.title}</NavigationMenu.Trigger>
                                    <NavigationMenu.Content>
                                        <ul class="grid w-75 gap-4 p-2">
                                            {#each route.subList as subRoute(subRoute.title)}
                                                <li>
                                                    <NavigationMenu.Link href={subRoute.href}>
                                                        <div class="font-medium">{subRoute.title}</div>
                                                    </NavigationMenu.Link>
                                                </li>
                                            {/each}
                                        </ul>
                                    </NavigationMenu.Content>
                                {:else}
                                    {#if route.href}
                                        <NavigationMenu.Link href={route.href}>
                                            {#snippet child()}
                                                <a href={route.href} class={cn("dark:bg-page-foreground", navigationMenuTriggerStyle(), page.url.href === route.href ? "font-bold" : "")}>{route.title}</a>
                                            {/snippet}
                                        </NavigationMenu.Link>
                                    {/if}
                                    {#each route.subList as subRoute(subRoute.title)}
                                        <NavigationMenu.Link href={subRoute.href}>
                                            {#snippet child()}
                                                <a href={subRoute.href} class={cn("dark:bg-page-foreground", navigationMenuTriggerStyle(), page.url.href === subRoute.href ? "font-bold" : "")}>{subRoute.title}</a>
                                            {/snippet}
                                        </NavigationMenu.Link>
                                    {/each}
                                {/if}
                            {:else}
                                <NavigationMenu.Link href={route.href}>
                                    {#snippet child()}
                                        <a href={route.href} class={cn("dark:bg-page-foreground", navigationMenuTriggerStyle(), page.url.href === route.href ? "font-bold" : "")}>{route.title}</a>
                                    {/snippet}
                                </NavigationMenu.Link>
                            {/if}
                        </NavigationMenu.Item>
                    {/each}
                </NavigationMenu.List>
            </NavigationMenu.Root>
    
            <div class="flex gap-2">
                <Button
                    id="theme-switch-btn"
                    size="icon"
                    class="ml-auto"
                    variant="ghost"
                    onclick={toggleMode}
                >
                    <LucideSun
                        class="h-[1.2rem] w-[1.2rem] rotate-0 scale-100 transition-all! dark:-rotate-90 dark:scale-0"
                    />
                    <LucideMoon
                        class="absolute h-[1.2rem] w-[1.2rem] rotate-90 scale-0 transition-all! dark:rotate-0 dark:scale-100"
                    />
                </Button>
                {#if SHOW_LOGOS_AND_REPO}
                    <Button
                        size="icon"
                        variant="ghost"
                        href={GITHUB_PATH}
                    >
                        {#if mode.current === 'dark'}
                            <img src={GithubWhiteLogo} alt="GitHub" class="h-4 aspect-square w-auto" />
                        {:else}
                            <img src={GithubLogo} alt="GitHub" class="h-4 aspect-square w-auto" />
                        {/if}
                    </Button>
                {/if}
                <Button
                    size="icon"
                    class="min-[1150px]:hidden"
                    variant="ghost"
                    onclick={() => isVerticalMenuOpen = !isVerticalMenuOpen}
                >
                    <LucideMenu
                        class={`h-[1.2rem] w-[1.2rem] transition-all! ${isVerticalMenuOpen ? "-rotate-90 scale-0" : "rotate-0 scale-100"}`}
                    />
                    <LucideX
                        class={`absolute h-[1.2rem] w-[1.2rem] transition-all! ${isVerticalMenuOpen ? "rotate-0 scale-100" : "rotate-90 scale-0"}`}
                    />
                </Button>
            </div>
        </div>
    
        <NavigationMenu.Root orientation="vertical" viewport={false} class={cn("min-[1150px]:hidden flex flex-col w-full overflow-hidden transition-all duration-500 ease-in-out", isVerticalMenuOpen ? "max-h-120" : "max-h-0")}>
            <NavigationMenu.List class="mx-5 gap-2 flex-col py-2">
                {#each routes as route(route.title)}
                        {#if route.subList}
                            {@const subList = route.href ? [route, ...route.subList] : route.subList}
                            {#each subList as subRoute(subRoute.title)}
                                <NavigationMenu.Item>
                                    <NavigationMenu.Link href={subRoute.href}>
                                        {#snippet child()}
                                            <a href={subRoute.href} onclick={() => isVerticalMenuOpen = false} class={cn("dark:bg-page-foreground", navigationMenuTriggerStyle(), page.url.href === subRoute.href ? "font-bold" : "")}>{subRoute.title}</a>
                                        {/snippet}
                                    </NavigationMenu.Link>
                                </NavigationMenu.Item>
                            {/each}
                        {:else}
                            <NavigationMenu.Item>   
                                <NavigationMenu.Link href={route.href}>
                                    {#snippet child()}
                                        <a href={route.href} onclick={() => isVerticalMenuOpen = false} class={cn("dark:bg-page-foreground", navigationMenuTriggerStyle(), page.url.href === route.href ? "font-bold" : "")}>{route.title}</a>
                                    {/snippet}
                                </NavigationMenu.Link>
                            </NavigationMenu.Item>
                        {/if}
                {/each}
            </NavigationMenu.List>
        </NavigationMenu.Root>
    </div>

    <div class="mt-30 px-10">
        <Breadcrumb.Root>
            <Breadcrumb.List>
                {#each breadcrumbs as crumb, i (crumb.title)}
                    <Breadcrumb.Item>
                        {#if i === breadcrumbs.length - 1}
                            <Breadcrumb.Page class="text-2xl font-semibold">{crumb.title}</Breadcrumb.Page>
                        {:else}
                            {#if crumb.href}
                                <Breadcrumb.Link class="text-2xl font-medium" href={crumb.href}>{crumb.title}</Breadcrumb.Link>
                            {:else}
                                <Breadcrumb.Page class="text-2xl">{crumb.title}</Breadcrumb.Page>
                            {/if}
                        {/if}
                    </Breadcrumb.Item>
                    {#if i < breadcrumbs.length - 1}
                        <ChevronRight class="h-5 w-5" />
                    {/if}
                {/each}
            </Breadcrumb.List>
        </Breadcrumb.Root>
    </div>
</header>
