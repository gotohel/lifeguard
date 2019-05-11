package team.gotohel.lifeguard.api

class RecipesResponse(val results: List<Recipes>?, val baseUri: String)
class Recipes(val id: Int?, val title: String?, val image: String?)