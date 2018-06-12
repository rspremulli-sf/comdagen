/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.generator

import com.salesforce.comdagen.Configuration
import com.salesforce.comdagen.RenderConfig
import com.salesforce.comdagen.model.AttributeDefinition
import java.util.*

/**
 * A wrapper for a specific object-template. It provides everything the template will need.
 *
 * For now, this is only an iterator on the underlying [ObjectClass]. The reason we can not provide a list is that these
 * can be in the order of millions of items.
 */
interface Generator<out C, out ObjectClass>
        where C : RenderConfig,
              C : Configuration {

    /**
     * Iterate over the result space for this object. It is understood that iteration will use a configuration specific
     * to the generator instance to control things like item amount, shape and data variance of the objects.
     *
     * We provide a default implementation because interactions between [Random] and [Sequence] have proven difficult to
     * get right: sequences can be iterated multiple times and `Random` instances would keep state between those
     * iterations, leading to two iterations producing different results.
     */
    val objects: Sequence<ObjectClass>
        get() {
            // generate master catalogs
            val rng = Random(configuration.initialSeed)
            // can not use rng inside the sequence (or it'll never generate the same ids)
            // so we save the seeds and use them as a starting point for the sequence
            val seeds = (1..configuration.elementCount).map { rng.nextLong() }

            return seeds.asSequence().mapIndexed { idx, seed -> creatorFunc(idx, seed) }
        }

    /**
     * Call this function to generate a single item for the [objects] sequence.
     *
     * The function will get two parameters: `idx`, the object position in the sequence, and `seed`, the random number
     * seed to use _for this object_.
     */
    val creatorFunc: (idx: Int, seed: Long) -> ObjectClass
        get() = TODO("Implement creatorFunc or overwrite objects accessor")

    /**
     * Provides the template name relative to the "config" directory this Generator suggests for ease of use.
     * What actually gets passed to the renderer is of no concern to the Generator.
     */
    val generatorTemplate: String
        get() = configuration.templateName

    /**
     * Provides the configuration for this object type.
     */
    val configuration: C

    /**
     * Attribute definitions for the object(s) generated by this instance.
     * This can be multiple object types when the generator produces a "top-level" object like catalogs that in turn
     * contain products.
     *
     * Main use is for generating custom attribute definitions in [XMLOutputProducer#renderMeta].
     */
    val metadata: Map<String, Set<AttributeDefinition>>
}
