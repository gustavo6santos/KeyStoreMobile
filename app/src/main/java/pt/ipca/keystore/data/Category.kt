package pt.ipca.keystore.data

sealed class Category(val category: String) {
    object Pc:Category("Pc")
    object Ps:Category("Ps")
    object Nintendo:Category("Nintendo")
    object Xbox:Category("Xbox")

}