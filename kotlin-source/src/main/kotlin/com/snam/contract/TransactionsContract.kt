package com.snam.contract

import com.snam.state.ProposalState
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
        println("transaction verify "+tx.toString())
        val commands = tx.commandsOfType<Commands>()
        for(command in commands){
            val setOfSigners = command.signers.toSet()
            when (command.value) {
                is Commands.Create -> verifyCreate(tx, setOfSigners)
                is Commands.Issue -> verifyIssue(tx, setOfSigners)
                else -> throw IllegalArgumentException("Unrecognised command.")
            }
        }
    }

    interface Commands : CommandData {
        class Create : Commands
        class Issue : Commands
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

    private fun verifyIssue(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {

        println("started verifyIssue")
        println(tx.toString())

        //proposal
        "there must be only one input" using (tx.inputStates.size == 1)
        val proposal = tx.inputsOfType<ProposalState>().single()

        //transaction
        "Only one transaction state should be created." using (tx.outputStates.size == 1)
        val transaction = tx.outputsOfType<TransactionState>().single()
        "seller and buyer cannot be the same" using (transaction.seller != transaction.buyer)
        //"date cannot be in the future" using (transaction.data < Date())
        "energy must be grather than 0" using (transaction.energia > 0)
        "pricePerUnit must be grather than 0" using (transaction.pricePerUnit > 0)
        "All of the participants must be signers." using (signers.containsAll(transaction.participants.map { it.owningKey }))

        //proposal and transaction
        "issuer must be seller of buyer" using (proposal.issuer == transaction.seller || proposal.issuer == transaction.buyer)
        "counterpart must be seller of buyer" using (proposal.counterpart == transaction.seller || proposal.counterpart == transaction.buyer)
        "energy must be equal" using (proposal.energia == transaction.energia)
        "priceperunit must be equal" using (proposal.pricePerUnit == transaction.pricePerUnit)
        "linearId must be acquisto o vendita" using (proposal.linearId.id.toString() == transaction.idAcquisto || proposal.linearId.id.toString() == transaction.idVendita)
    }
}
