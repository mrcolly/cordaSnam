package com.snam.contract


import com.snam.state.ProposalState
import com.snam.state.TransactionState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey
import java.util.*


class ProposalContract : Contract {
    companion object {
        @JvmStatic
        val PROPOSAL_CONTRACT_ID = "com.snam.contract.ProposalContract"
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
        val proposal = tx.outputsOfType<ProposalState>().single()
        "issuer and counterpart cannot be the same" using (proposal.issuer != proposal.counterpart)
        "energy must be grather than 0" using (proposal.energia > 0)
        "pricePerUnit must be grather than 0" using (proposal.pricePerUnit > 0)
        "validity must be grather than date" using (proposal.validity > proposal.data)
        "proposal type must be 'A' or 'V'" using (proposal.type === 'V' || proposal.type === 'A')
        "All of the participants must be signers." using (signers.containsAll(proposal.participants.map { it.owningKey }))
    }
}
