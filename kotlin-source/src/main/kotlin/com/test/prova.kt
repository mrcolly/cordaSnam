package com.test

import com.snam.flow.ProposalFlow

fun main(args: Array<String>) {
    println(ProposalFlow.checkBalance("EMI", 12.0))
    println(ProposalFlow.checkBalance("EMI", 6000.0))
    ProposalFlow.updateBalance("EMI", 333.0)
    println(ProposalFlow.checkBalance("EMI", 6000.0))
}