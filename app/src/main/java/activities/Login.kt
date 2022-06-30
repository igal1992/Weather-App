package activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import xd.activities.R
import xd.activities.databinding.ActivityLoginBinding
import xd.activities.databinding.ActivityRegisterBinding

class Login : AppCompatActivity() {

    private lateinit var  binding: ActivityLoginBinding
    private lateinit var  firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        //get an instance from the db
        firebaseAuth = FirebaseAuth.getInstance()

        //create a listener for the register now button to move to the register activity
        binding.registerNowButton.setOnClickListener{
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        //create the listener for the login button
        binding.loginButton.setOnClickListener {
            this.login()
        }
    }
    private fun login(){
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()

        //check if all fields are filled
        if(email.isNotEmpty() && password.isNotEmpty() ){
            //login to user with username field and password field
            firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener{
                if(it.isSuccessful){//if successfully logged in switch activity to main screen
                    val intent = Intent(this, activities.MainActivity::class.java)
                    startActivity(intent)
                }else{//else make an error message
                    Toast.makeText(this,it.exception.toString() , Toast.LENGTH_SHORT).show()
                }
            }
        }else{//else make an error message
            Toast.makeText(this,"Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()
        }
    }
    //check on start up if user logged in if so go to main screen
    override fun onStart() {
        super.onStart()
        if(firebaseAuth.currentUser != null){
            val intent = Intent(this, activities.MainActivity::class.java)
            startActivity(intent)
        }
    }
}