package id.mamalele.bukukas

data class Transaction(
    val id: Long,
    val date: String,
    val type: String,
    val note: String,
    val amount: Long
)
