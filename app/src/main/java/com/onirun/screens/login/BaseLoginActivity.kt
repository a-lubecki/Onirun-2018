package com.onirun.screens.login

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.internal.CallbackManagerImpl
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.mcxiaoke.koi.log.logw
import com.onirun.R
import com.onirun.api.APIManager
import com.onirun.api.UserManager
import com.onirun.screens.main.BaseLoadingActivity
import com.onirun.screens.splash.ConfigureActivity
import com.onirun.utils.alertDialog
import com.onirun.utils.showIfActivityRunning
import com.onirun.utils.trackEvent


abstract class BaseLoginActivity : BaseLoadingActivity() {


    companion object {

        private val REQUEST_CODE_FACEBOOK = CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()
        private const val REQUEST_CODE_GOOGLE = 1753
    }


    private var callbackManager: CallbackManager? = null

    private val googleApiClient by lazy {

        //initialize google sign in
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        GoogleSignIn.getClient(this, options)
    }

    protected val auth by lazy {
        FirebaseAuth.getInstance()!!
    }

    protected val textPasswordAdvice by lazy {

        val text = getString(
                R.string.login_password_hint,
                getString(R.string.login_password_condition0),
                getString(R.string.login_password_condition1),
                getString(R.string.login_password_condition2)
        )

        SpannableString(text).also {
            addColorSpan(it, R.string.login_password_condition0)
            addColorSpan(it, R.string.login_password_condition1)
            addColorSpan(it, R.string.login_password_condition2)
        }
    }

    private fun addColorSpan(text: SpannableString, subTextId: Int) {

        val subText = getString(subTextId)
        val index = text.indexOf(subText)

        text.setSpan(ForegroundColorSpan(
                ContextCompat.getColor(this@BaseLoginActivity, R.color.textTitle)),
                index,
                index + subText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (auth.currentUser != null && !auth.currentUser!!.isAnonymous) {
            throw IllegalStateException("Trying to logging in a user already logged")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REQUEST_CODE_FACEBOOK) {

            //manage facebook callback
            callbackManager?.onActivityResult(requestCode, resultCode, data)

            callbackManager = null

        } else if (requestCode == REQUEST_CODE_GOOGLE) {

            endLoginViaGoogle(GoogleSignIn.getSignedInAccountFromIntent(data))
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun isPasswordSyntaxCorrect(password: String): Boolean {

        if (password.length < 8) {
            return false
        }

        if (password.firstOrNull { it.isUpperCase() } == null) {
            return false
        }

        if (password.firstOrNull { it.isDigit() } == null) {
            return false
        }

        return true
    }

    protected fun checkIfAccountExists(email: String?, completion: (Boolean) -> Unit) {

        if (email.isNullOrBlank()) {
            return
        }

        hideKeyboard()
        showLoaderDelayed()

        //if sign up + existing user (anonymous) => find in the db is the email is already present, if not : the user try to sign in with a non-registered email
        auth.fetchSignInMethodsForEmail(email)
                .addOnSuccessListener {

                    hideLoader()

                    val signInMethods = it.signInMethods
                    val isExisting = signInMethods != null && signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)

                    //if this email has been registered once => sign in, else sign up
                    completion(isExisting)

                }
                .addOnFailureListener {
                    displayLoginError(it)
                }

    }

    protected fun signInViaMail(email: String?, password: String?) {

        if (email.isNullOrBlank()) {
            return
        }

        if (password.isNullOrBlank()) {
            return
        }

        hideKeyboard()
        showLoaderDelayed()

        //default sign in
        firebaseEmailSignIn(email, password)
    }

    protected fun signUpViaMail(email: String?, password: String?) {

        if (email.isNullOrBlank()) {
            return
        }

        if (password.isNullOrBlank()) {
            return
        }

        //check for password before sign up
        if (!isPasswordSyntaxCorrect(password)) {

            alertDialog()
                    .setTitle(R.string.error_title_login)
                    .setMessage(textPasswordAdvice)
                    .setNegativeButton(android.R.string.ok, null)
                    .showIfActivityRunning()
            return
        }

        hideKeyboard()
        showLoaderDelayed()

        if (auth.currentUser == null) {
            //default sign up
            firebaseEmailSignUp(email, password)

        } else {

            //link the anonymous credential with the email credential for this account
            firebaseLinkWithCredential(EmailAuthProvider.getCredential(email, password)) { _, isSignUp ->
                startNextActivity(isSignUp)
            }
        }
    }

    protected fun loginViaFacebook() {

        LoginManager.getInstance().logOut()

        hideKeyboard()
        showLoaderDelayed()

        callbackManager = CallbackManager.Factory.create()

        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {

            override fun onSuccess(loginResult: LoginResult) {

                val token = loginResult.accessToken?.token
                if (token == null) {
                    displayLoginError(getString(R.string.error_login_fb))
                    return
                }

                //retrieve email from facebook then login to firebase only if the email exists
                GraphRequest.newMeRequest(loginResult.accessToken) { jsonObject, response ->

                    if (response.error != null) {
                        displayLoginError(getString(R.string.error_login_fb))
                        return@newMeRequest
                    }

                    val email = jsonObject.optString("email")
                    if (email.isNullOrEmpty()) {
                        displayLoginError(getString(R.string.error_login_fb_email_missing))
                        return@newMeRequest
                    }

                    //the user has an email, login with firebase
                    loginWithCredential(FacebookAuthProvider.getCredential(token))

                }.apply {

                    parameters = Bundle().apply { putString("fields", "email") }
                    executeAsync()
                }
            }

            override fun onCancel() {
                hideLoader()
            }

            override fun onError(exception: FacebookException) {
                displayLoginError(getString(R.string.error_login_fb))
            }
        })

        LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"))
    }

    protected fun loginViaGoogle() {

        googleApiClient.signOut()

        hideKeyboard()
        showLoaderDelayed()

        val signInIntent = googleApiClient.signInIntent
        startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE)
    }

    private fun endLoginViaGoogle(result: Task<GoogleSignInAccount>) {

        if (result.isCanceled) {
            //do nothing
            hideLoader()
            return
        }

        var idToken: String? = null

        try {
            val account = result.getResult(ApiException::class.java)

            idToken = account?.idToken

        } catch (e: ApiException) {

            logw(javaClass.name, e)

            if (e.statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                //cancelled by the user
                hideLoader()
                return
            }

            if (e.statusCode == GoogleSignInStatusCodes.SIGN_IN_FAILED) {
                //impossible to connect with this account
                displayLoginError(getString(R.string.error_login_google))
                return
            }
        }

        if (idToken == null) {
            displayLoginError()
            return
        }

        loginWithCredential(GoogleAuthProvider.getCredential(idToken, null))
    }

    private fun loginWithCredential(credential: AuthCredential) {

        if (auth.currentUser != null && auth.currentUser!!.isAnonymous) {

            //if existing user (anonymous) => link instead of signing in
            firebaseLinkWithCredential(credential) { _, isSignUp ->

                retrieveTermsConsentAfterSocialLogin(isSignUp)
            }

        } else {

            //no existing user, default sign in
            firebaseSignInWithCredential(credential) { _, isSignUp ->

                retrieveTermsConsentAfterSocialLogin(isSignUp)
            }
        }
    }

    private fun retrieveTermsConsentAfterSocialLogin(isSignUp: Boolean) {

        //bug with firebase : the token is still linked to the anonymous account => invalidate it
        UserManager.resetLocalUserData(this)

        if (UserManager.hasAcceptedTerms(this)) {

            startNextActivity(false)
            return
        }

        APIManager.call(
                this,
                {
                    it.getUserTermsConsent()
                },
                true,
                false,
                { accepted ->

                    UserManager.updateTermsAccepted(this, accepted)

                    if (accepted) {
                        startNextActivity(isSignUp)
                    } else {
                        SignUpConsentActivity.start(this)
                    }
                },
                {

                    //show signup even if already accepted
                    SignUpConsentActivity.start(this)
                }
        )

    }

    private fun firebaseEmailSignIn(email: String, password: String) {

        auth.signOut()

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {

                    //bug with firebase : the token is still linked to the anonymous account => invalidate it
                    UserManager.resetLocalUserData(this)

                    startNextActivity(false)

                    trackEvent("email_login")
                }
                .addOnFailureListener {

                    displayLoginError(it)
                }
    }

    private fun firebaseEmailSignUp(email: String, password: String) {

        auth.signOut()

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {

                    //bug with firebase : the token is still linked to the anonymous account => invalidate it
                    UserManager.resetLocalUserData(this)

                    startNextActivity(true)

                    trackEvent("email_subscription")
                }
                .addOnFailureListener {

                    displayLoginError(it)
                }
    }

    private fun firebaseLinkWithCredential(credential: AuthCredential, onSuccess: (AuthResult, isSignUp: Boolean) -> Unit) {

        //used only if the user has been registered anonymously
        auth.currentUser!!.linkWithCredential(credential)
                .addOnSuccessListener {

                    UserManager.resetLocalUserData(this)

                    onSuccess(it, true)

                    when (credential) {

                        is FacebookAuthCredential -> trackEvent("facebook_subscription")

                        is GoogleAuthCredential -> trackEvent("google_subscription")

                        is EmailAuthCredential -> trackEvent("email_subscription")
                    }
                }
                .addOnFailureListener {

                    if (it is FirebaseAuthUserCollisionException) {

                        //the account is already existing, just sign in
                        firebaseSignInWithCredential(credential, onSuccess)
                        return@addOnFailureListener
                    }

                    //problem with new account
                    displayLoginError(it)
                }
    }

    private fun firebaseSignInWithCredential(credential: AuthCredential, onSuccess: (AuthResult, isSignUp: Boolean) -> Unit) {

        auth.signOut()

        auth.signInWithCredential(credential)
                .addOnSuccessListener {

                    UserManager.resetLocalUserData(this)

                    onSuccess(it, false)

                    if (credential is FacebookAuthCredential) {
                        trackEvent("facebook_login")
                    } else {
                        trackEvent("google_login")
                    }
                }
                .addOnFailureListener {

                    displayLoginError(it)
                }
    }

    protected fun startNextActivity(isSignUp: Boolean) {

        hideLoader()

        if (isSignUp && !UserManager.hasAcceptedTerms(this)) {
            SignUpConsentActivity.start(this)
        } else {
            ConfigureActivity.startNewTask(this, isSignUp)
        }
    }


    protected fun displayLoginError(e: Throwable?) {

        val errorMessage = e?.localizedMessage ?: return

        displayLoginError(errorMessage)
    }

    protected fun displayLoginError(errorMessage: String? = getString(R.string.error_message_default)) {

        hideLoader()

        alertDialog()
                .setTitle(R.string.error_title_login)
                .setMessage(errorMessage)
                .setNegativeButton(android.R.string.ok, null)
                .showIfActivityRunning()
    }

}