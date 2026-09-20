package com.fati_market

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fati_market.ui.components.*
import com.fati_market.ui.theme.LocalMarketAccents
import com.fati_market.ui.theme.Spacing

/**
 * Everything an item's own row cannot say.
 *
 * The inventory screens showed a price, a seller and a markup, and the item
 * detail page stopped in the same place: the agreed price, the payout, the
 * counter's photographs and the sale that ended a sold listing could only be
 * read on a computer. This is the website's item window, in the order it
 * reads there - the sale first when there is one, then the store's figures,
 * then the proof.
 */
@Composable
internal fun AdminItemFactsCard(item: Item, modifier: Modifier = Modifier) {
    val accents = LocalMarketAccents.current
    val sale = item.sale

    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (sale != null) {
            MarketCard {
                Overline("The sale")
                Spacer(Modifier.height(Spacing.xs))

                if (sale.receiptNo.isNotBlank()) {
                    SummaryRow("Receipt", sale.receiptNo)
                }

                val buyer = sale.buyerName.ifBlank { sale.buyerEmail }
                if (buyer.isNotBlank()) {
                    SummaryRow("Buyer", buyer)
                }

                if (sale.paymentMethod.isNotBlank()) {
                    SummaryRow("Payment method", sale.paymentMethod.replaceFirstChar { it.uppercaseChar() })
                }

                if (sale.paymentStatus.isNotBlank()) {
                    SummaryRow(
                        "Payment status",
                        sale.paymentStatus.replace('_', ' ').replaceFirstChar { it.uppercaseChar() },
                    )
                }

                SummaryRow("Item price", Money.format(sale.subtotal))

                if (sale.pointsUsed > 0) {
                    SummaryRow(
                        label = "${sale.pointsUsed} point(s) used",
                        value = "-" + Money.format(sale.pointsDiscountAmount),
                        valueColor = accents.reward,
                    )
                }

                SummaryRow("Amount paid", Money.format(sale.amountDue), emphasized = true)

                SummaryRow(
                    label = "Buyer earned",
                    value = if (sale.rewardPointsEarned > 0) "${sale.rewardPointsEarned} point(s)" else "No points",
                    valueColor = if (sale.rewardPointsEarned > 0) accents.reward else null,
                )

                Dates.short(sale.completedAt)?.let { SummaryRow("Sold on", it) }
            }
        }

        MarketCard {
            Overline("Figures")
            Spacer(Modifier.height(Spacing.xs))

            SummaryRow("Asking price", Money.format(item.sellerAskingPrice))
            SummaryRow("Agreed price", Money.format(item.acquisitionPrice))
            SummaryRow("Selling price", Money.format(item.publicPrice))

            if (item.markup != null) {
                SummaryRow("Markup", Money.format(item.markup), valueColor = accents.success)
            }

            if (item.rewardPoints > 0) {
                SummaryRow("Buyer earns", "${item.rewardPoints} point(s)", valueColor = accents.reward)
            }

            SoftDivider()

            SummaryRow("Received", if (item.isTurnoverVerified) "Yes" else "Not yet")

            SummaryRow(
                label = "Seller payout",
                value = if (item.sellerIsPaid) {
                    "Paid " + Money.format(item.sellerPayoutAmount ?: item.acquisitionPrice)
                } else {
                    "Not paid yet"
                },
                valueColor = if (item.sellerIsPaid) accents.success else accents.warning,
            )

            Dates.short(item.acquiredAt)?.let { SummaryRow("Received on", it) }
            Dates.short(item.publishedAt)?.let { SummaryRow("Published on", it) }

            item.rejectedReason?.let { reason ->
                SoftDivider()
                SummaryRow("Declined because", reason)
            }
        }

        if (item.turnoverPhoto != null || item.sellerPayoutPhoto != null) {
            MarketCard {
                Overline("The counter's proof")
                Spacer(Modifier.height(Spacing.xs))

                Text(
                    "Taken when the item came in: the seller with their cash, and the item itself.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(Spacing.sm))

                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    item.sellerPayoutPhoto?.let { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = "The seller being paid",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(1f)
                                .height(130.dp)
                                .clip(RoundedCornerShape(10.dp)),
                        )
                    }

                    item.turnoverPhoto?.let { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = "The item received",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(1f)
                                .height(130.dp)
                                .clip(RoundedCornerShape(10.dp)),
                        )
                    }
                }
            }
        }
    }
}

/**
 * The sale, squeezed onto the row that sold it: who bought it, what they
 * actually paid, how, and what they earned back.
 */
@Composable
internal fun SoldRowFacts(sale: ItemSale) {
    val buyer = sale.buyerName.ifBlank { sale.buyerEmail }

    if (buyer.isNotBlank()) {
        Text(
            "Bought by $buyer",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        StatusPill(label = "Paid " + Money.format(sale.amountDue), tone = StatusTone.Success)

        if (sale.paymentMethod.isNotBlank()) {
            StatusPill(
                label = sale.paymentMethod.replaceFirstChar { it.uppercaseChar() },
                tone = StatusTone.Info,
            )
        }

        if (sale.rewardPointsEarned > 0) {
            StatusPill(label = "+${sale.rewardPointsEarned} pt", tone = StatusTone.Warning)
        }
    }
}
