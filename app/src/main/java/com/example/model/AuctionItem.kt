package com.example.model

data class AuctionItem(
    val id: String,
    val title: String,
    val description: String,
    val currentBid: Double,
    val remainingSeconds: Long,
    val highestBidder: String = "anunciante_top",
    val totalBids: Int = 42,
    val iconType: String = "home"
) {
    val formattedBid: String
        get() = String.format("R$ %.2f", currentBid).replace(".", ",")

    val formattedRemainingTime: String
        get() {
            val hours = remainingSeconds / 3600
            val minutes = (remainingSeconds % 3600) / 60
            val seconds = remainingSeconds % 60
            return if (hours > 0) {
                String.format("%02dh %02dm %02ds", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }
}

data class BidSubmission(
    val auctionId: String,
    val email: String,
    val amount: Double,
    val adTitle: String = ""
)
