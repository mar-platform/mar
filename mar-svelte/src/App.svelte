<script>
  	import { Router, Link, Route } from "svelte-routing";
  	import Index  from "./routes/Index.svelte";
  	import About  from "./routes/About.svelte";
  	import Status from "./routes/Status.svelte";

	// For the router
	export let url = "";
</script>

<Router url="{url}">
	<nav class="navbar navbar-expand-lg navbar-light bg-light">
		<div class="d-flex flex-grow-1">
			<Link class="navbar-brand" to="/">
				<img
					src="img/logo_mar.png"
					width="30"
					height="30"
					class="d-inline-block align-top"
					alt="" />
				MAR
			</Link>
		</div>
		<div
			class="collapse navbar-collapse flex-grow-1 text-right"
			id="myNavbar">
			<ul class="navbar-nav ml-auto flex-nowrap">
				<li class="nav-item">
					<Link
						to="status"
						class="nav-link m-2 menu-item nav-active">Status</Link>
				</li>
				<li class="nav-item">
					<Link
						to="about"
						class="nav-link m-2 menu-item nav-active" >About</Link>
				</li>
			</ul>
		</div>
	</nav>
	<section class="masthead">
		<!-- <div class="overlay" /> -->
		<!-- <div class="container text-white"> -->

		<div class="container-fluid">
			<div>
				<!-- <Route path="blog/:id" component="{BlogPost}" />
				<Route path="blog" component="{Blog}" /> -->

				<Route path="about" component="{About}" />
				<Route path="status" component="{Status}" />
				<Route path="/"><Index /></Route>
			</div>	
		</div>
	</section>
</Router>