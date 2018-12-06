package com.snam.contract

import com.snam.state.TransactionState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey
import java.util.*


class TransactionsContract : Contract {
    companion object {
        @JvmStatic
        val TRANSACTION_CONTRACT_ID = "com.snam.contract.TransactionsContract"
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        val setOfSigners = command.signers.toSet()
        when (command.value) {
            is Commands.Create -> verifyCreate(tx, setOfSigners)
            else -> throw IllegalArgumentException("Unrecognised command.")
        }
    }

    interface Commands : CommandData {
        class Create : TypeOnlyCommandData(), Commands
        //class Settle : TypeOnlyCommandData(), Commands
    }

    private fun verifyCreate(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        "No inputs should be consumed when creating a transaction." using (tx.inputStates.isEmpty())
        "Only one transaction state should be created." using (tx.outputStates.size == 1)
        val transaction = tx.outputsOfType<TransactionState>().single()
        "seller and buyer cannot be the same" using (transaction.seller != transaction.buyer)
        "date cannot be in the future" using (transaction.data < Date())
        "energy must be grather than 0" using (transaction.energia > 0)
        "pricePerUnit must be grather than 0" using (transaction.pricePerUnit > 0)
        "All of the participants must be signers." using (signers.containsAll(transaction.participants.map { it.owningKey }))
    }
}
