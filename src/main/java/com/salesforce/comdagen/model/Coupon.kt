/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.model

import com.salesforce.comdagen.RandomData
import com.salesforce.comdagen.config.CouponConfiguration
import com.salesforce.comdagen.config.SystemCodeConfig
import java.util.*

abstract class Coupon(private val seed: Long) {
    val id: String
        get() = "comdagen-${Math.abs(seed)}"

    val description: String
        get() = RandomData.getRandomSentence(seed + "description".hashCode())

    val enabled: Boolean
        get() = true
}

/**
 * Coupon with one pregenerated code
 */
class SingleCodeCoupon(private val seed: Long) : Coupon(seed) {
    val singleCode: String
        get() = RandomData.getRandomCouponCode(seed)
}

/**
 * Coupon with a list of pregenerated codes
 */
class CodeListCoupon(private val seed: Long, private val config: CouponConfiguration) : Coupon(seed) {

    val codeList: List<String>
        get() {
            val rng = Random(seed)
            val count = rng.nextInt(config.maxCodes - config.minCodes) + config.minCodes

            return (1..count).map { RandomData.getRandomCouponCode(rng.nextLong()) }
        }
}

/**
 * Coupon which codes get generated by Commerce Cloud
 */
class SystemCodeCoupon(private val seed: Long, private val config: SystemCodeConfig) : Coupon(seed) {
    val systemCodes: SystemCodes
        get() {
            val maxNumberOfCodes = Random(seed).nextInt(config.maxCodes - config.minCodes) + config.minCodes

            return SystemCodes(maxNumberOfCodes)
        }
}

data class SystemCodes(val maxNumberOfCodes: Int)