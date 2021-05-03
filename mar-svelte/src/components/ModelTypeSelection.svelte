<script>
    export var selected = "Ecore";

    function buttonStyle(selected, type) {
        var active = (selected == type) ? 'active' : '';
        return `btn btn-secondary mr-2 ${active} btn-model-type`
    }

    let ecoreStyle, umlStyle

    $: ecoreStyle = buttonStyle(selected, 'Ecore');
    $: umlStyle   = buttonStyle(selected, 'UML');
    $: bpmn2Style   = buttonStyle(selected, 'BPMN2');
    $: pnmlStyle   = buttonStyle(selected, 'PNML');
    $: sculptorStyle = buttonStyle(selected, 'Sculptor');
    $: rdsStyle = buttonStyle(selected, 'RDS');
    $: simulinkStyle = buttonStyle(selected, 'Simulink');
    $: archimateStyle = buttonStyle(selected, 'Archimate');        
</script>

<main>
    <div
        class="alert alert-info"
        style="width: 100%; padding: 4px; padding-left: 8px"
        role="alert">
        Select the desired syntax
    </div>
    <div class="btn-group btn-group-sm" style="display: block" role="group" aria-label="Basic example">
        <!-- <span class="badge badge-light">Model type</span> -->
        <button
            type="button"
            class="{ecoreStyle}"
            on:click={() => (selected = 'Ecore')}>Ecore</button>
        <button
            type="button"
            class="{umlStyle}"
            on:click={() => (selected = 'UML')}>UML</button>
        <button
            type="button"
            class="{bpmn2Style}"
            on:click={() => (selected = 'BPMN2')}>BPMN2</button>
        <button
            type="button"
            class="{pnmlStyle}"
            on:click={() => (selected = 'PNML')}>PNML</button>
        <button
            type="button"
            class="{sculptorStyle}"
            on:click={() => (selected = 'Sculptor')}>Sculptor</button>
        <button
            type="button"
            class="{rdsStyle}"
            on:click={() => (selected = 'RDS')}>RDS</button>
        <button
            type="button"
            class="{simulinkStyle}"
            on:click={() => (selected = 'Simulink')}>Simulink</button>            
        <button
            type="button"
            class="{archimateStyle}"
            on:click={() => (selected = 'Archimate')}>Archimate</button>
    </div>
</main>
