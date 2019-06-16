package org.juicechain.models;

public enum AssetType{
    admission,  // Ticket, Entry pass
    voucher,    // Redeemable transaction type which is worth a certain monetary value
    coupon,     // Redeemed for a financial discount
    contract,   // Agreement between multiple parties
    identity    // Piece of identification or ID
}
