package india.ajay.smartlockapplication

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import com.google.android.gms.auth.api.credentials.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.common.api.ResultCallback;
import india.ajay.smartlockapplication.Constants.RC_HINT

class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<CredentialRequestResult> {
    
    private var credentialClient: CredentialsClient? = null
    private var credentialRequest: CredentialRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        credentialClient = Credentials.getClient(this)
        credentialRequest = CredentialRequest.Builder().setPasswordLoginSupported(true)
                .setAccountTypes(IdentityProviders.GOOGLE, IdentityProviders.TWITTER)
                .build()

        credentialClient?.request(credentialRequest!!)?.addOnCompleteListener(object: OnCompleteListener<CredentialRequestResponse>{
            override fun onComplete(task: Task<CredentialRequestResponse>) {
                if(task.isSuccessful){
                    task.result?.credential?.let { onCredentialRetrieved(it)}
                    return
                }
                val exception = task.exception
                if(exception is ResolvableApiException){
                    val resoleApiException = exception as ResolvableApiException
                    resolveResult(resoleApiException, Constants.RC_READ)
                } else if(exception is ApiException){
                    Log.e("TAG", "Unsuccessful credential request.", exception)
                    val ae = exception as ApiException
                    val code = ae.statusCode
                }
            }
        })

    }

    override fun onConnected(p0: Bundle?) {

    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    override fun onResult(p0: CredentialRequestResult) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == Constants.RC_READ){
            if(resultCode == Activity.RESULT_OK){
                val credential = data?.getParcelableExtra<Credential>(Credential.EXTRA_KEY)
                onCredentialRetrieved(credential)
            } else {
                Log.e("TAG", "Credential Read Not Ok")
                Toast.makeText(this, "Credential Read Failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onCredentialRetrieved(credential: Credential?){
        val accountType = credential?.accountType
        if(accountType == null){

        } else if(accountType.equals(IdentityProviders.GOOGLE)){
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
            val signInClient  = GoogleSignIn.getClient(this, gso)
            val task: Task<GoogleSignInAccount> = signInClient.silentSignIn()

            task.addOnCompleteListener {

            }

        }
    }

    private fun resolveResult(resolveException: ResolvableApiException, requestCode: Int){
        try{

        }catch (e: IntentSender.SendIntentException){
            Log.e("TAG", "Failed to send resolution", e)
            hideProgress()
        }
    }

    private fun hideProgress() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun showHintToUser(){
        val hintRequest: HintRequest = HintRequest.Builder()
            .setHintPickerConfig(CredentialPickerConfig.Builder()
                .setShowCancelButton(true).build())
            .setEmailAddressIdentifierSupported(true)
            .setAccountTypes(IdentityProviders.GOOGLE)
            .build()
        val pendingIntent = credentialClient?.getHintPickerIntent(hintRequest)
        try {
            startIntentSenderForResult(pendingIntent?.intentSender, RC_HINT, null, 0, 0, 0)
        } catch(intentSenderException : IntentSender.SendIntentException){
            Log.e("TAG", "Could not start hint picker Intent", intentSenderException);
        }
    }

}
