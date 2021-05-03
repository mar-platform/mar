<script>
    export let label
    export let minDefault
    export let maxDefault

    export let value
</script>

<!-- https://mdbootstrap.com/docs/standard/forms/range/ -->
<div class="form-group">
    <label for="formControlRange">{label} {value}</label>
    <input
        type="range"
        class="form-control-range"
        min="{minDefault}"
        max={maxDefault}
        bind:value={value}
        id="formControlRange"
    />
</div>
