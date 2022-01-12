package com.fsck.k9.activity.authError

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.fsck.k9.Account
import com.fsck.k9.ui.R
import com.fsck.k9.ui.authError.AuthenticationErrorViewModel
import com.fsck.k9.ui.authError.UiState
import com.fsck.k9.ui.base.K9Activity
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel

class AuthenticationError : K9Activity() {

    private val authenticationErrorViewModel: AuthenticationErrorViewModel by viewModel()

    private var accountUuid: String? = null
    private var incoming: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.authentication_error)

        accountUuid = intent.getStringExtra(ACCOUNT_UUID)
        incoming = intent.getBooleanExtra(INCOMING, false)

        lifecycleScope.launchWhenCreated {
            authenticationErrorViewModel.uiState.collect(::handleUiState)
        }

        authenticationErrorViewModel.onAccountSupplied(accountUuid)
        findViewById<Button>(R.id.authenticationErrorDismissBt).setOnClickListener { authenticationErrorViewModel.onClose() }
    }

    private fun handleUiState(uiState: UiState) {
        handleReconnectButtonVisibility(uiState.shouldShowReauthorize)
        handleEditServerSettingsButtonVisibility(uiState.shouldShowEditServerSettings)
        handleAccountErrorVisibility(uiState.shouldShowAccountUuidError)
        handleAuthorizationDescription(uiState.userName, uiState.isOAuth)
    }

    private fun handleReconnectButtonVisibility(isVisible: Boolean) {
        findViewById<Button>(R.id.authenticationErrorReAuthorizeBt).apply {
            this.isVisible = isVisible
            setOnClickListener { authenticationErrorViewModel.onOauthErrorAction(accountUuid) }
        }
    }

    private fun handleEditServerSettingsButtonVisibility(isVisible: Boolean) {
        findViewById<Button>(R.id.authenticationErrorServerSettingsBt).apply {
            this.isVisible = isVisible
            setOnClickListener {
                authenticationErrorViewModel.onServerErrorAction(accountUuid, incoming)
                this@AuthenticationError.finish()
            }
        }
    }

    private fun handleAccountErrorVisibility(isVisible: Boolean) {
        findViewById<TextView>(R.id.authenticationErrorDescriptionTv).run {
            text = if (isVisible) getString(R.string.authentication_error_account_error_text) else ""
        }
    }

    private fun handleAuthorizationDescription(userName: String, isOAuth: Boolean) {
        findViewById<TextView>(R.id.authenticationErrorDescriptionTv).run {
            text = if (isOAuth) {
                getString(R.string.authentication_error_description_oauth, userName)
            } else {
                getString(R.string.authentication_error_description_other, userName)
            }
        }
    }

    companion object {

        private const val ACCOUNT_UUID = "account_uuid"
        private const val INCOMING = "incoming"

        @JvmStatic
        fun authErrorIntent(context: Context?, account: Account, incoming: Boolean) =
            Intent(context, AuthenticationError::class.java).apply {
                putExtra(ACCOUNT_UUID, account.uuid)
                putExtra(INCOMING, incoming)
            }
    }
}