package com.neo4j.driver.kotlin.util

/**
 * Represents a single protocol revision as defined by the Bolt protocol specification.
 *
 * Revisions are separated into a [major] and [minor] bit respectively but may additionally carry a [range] which
 * extends their respective matching pattern to include a certain range of minor versions. For instance, a protocol
 * version of `4.0` with a range of `3` would include versions `4.0`, `4.1`, `4.2` and `4.3` respectively.
 *
 * Additionally, revisions are comparable in accordance with their respective logical release order. For the purposes of
 * this definition, the major bit will take precedence (thus making version `4.0` "larger" than version `3.5`).
 *
 * Larger protocol revisions generally indicate richer feature sets and support in more modern versions of the server
 * implementation (as prior versions are typically dropped once their respective support lifecycle has ended).
 */
inline class ProtocolVersion(val value: Int) : Comparable<ProtocolVersion> {

    /**
     * Identifies the major protocol version encoded within this value.
     */
    val major: Int
        get() = this.value and 0xFF

    /**
     * Identifies the minor protocol vesion encoded within this value.
     */
    val minor: Int
        get() = this.value ushr 8 and 0xFF

    /**
     * Defines an acceptable range of versions following the base version.
     *
     * This field may be used in order to indicate support for a broader range of minor versions (such as `4.0` through
     * `4.3` via a range of `3`).
     */
    val range: Int
        get() = this.value ushr 16 and 0xFF

    /**
     * Evaluates whether a given target revision matched this revision.
     *
     * This method will typically evaluate whether the version numbers match exactly unless a [range] parameter is given
     * in order to extend the range to additional minor revisions. In this case, the range will be considered as an
     * extension to the [minor] bit.
     */
    operator fun contains(other: ProtocolVersion): Boolean {
        if (this.range == 0) {
            return this == other
        }

        return this.major == other.major && this.minor in this.minor..(this.minor + this.range)
    }

    override fun compareTo(other: ProtocolVersion): Int {
        val majorComparison = this.major.compareTo(other.major)
        if (majorComparison != 0) {
            return majorComparison
        }

        return this.minor.compareTo(other.minor)
    }

    override fun toString() = if (this.range == 0) {
        "%d.%d".format(this.major, this.minor)
    } else {
        "%d.%d-%d.%d".format(this.major, this.minor, this.major, this.minor + this.range)
    }

    companion object {

        /**
         * Marker revision for padding purposes.
         *
         * This value is passed by clients when less than four protocol revisions are supported. In this case, the
         * remaining slots within the negotiation message are filled with this padding value in order to mark the end of
         * the list of supported revisions.
         */
        val padding = ProtocolVersion(0)

        /**
         * Marker revision for unsupported protocol revisions.
         *
         * This value is returned by servers when none of the presented protocol revisions is supported thus indicating
         * a negotiation error. Within this context, this version will indicate immediate termination of the connection.
         */
        val unsupported = ProtocolVersion(0)
    }

    /**
     * Creates a new protocol revision based on the given components.
     *
     * When no [range] is given to this constructor, an exact version reference is created instead thus matching only
     * that specific version within comparisons.
     */
    constructor(major: Int, minor: Int, range: Int = 0) : this(
        major or
                (minor shl 8) or
                (range shl 16)
    )
}