package activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import xd.activities.databinding.ActivityLoginBinding

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var regIntent: Intent
    private lateinit var email: String
    private lateinit var password : String
    private lateinit var mainIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        //get an instance from the db
        firebaseAuth = FirebaseAuth.getInstance()

        //create a listener for the register now button to move to the register activity
        binding.registerNowButton.setOnClickListener{
            regIntent = Intent(this, Register::class.java)
            startActivity(regIntent)
        }

        //create the listener for the login button
        binding.loginButton.setOnClickListener {
            this.login()
        }
    }
    private fun login(){
        email = binding.email.text.toString()
        password = binding.password.text.toString()

        //check if all fields are filled
        if(email.isNotEmpty() && password.isNotEmpty() ){
            //login to user with username field and password field
            firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener{
                if(it.isSuccessful){//if successfully logged in switch activity to main screen
                    mainIntent = Intent(this, activities.MainActivity::class.java)
                    startActivity(mainIntent)
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
            mainIntent = Intent(this, activities.MainActivity::class.java)
            startActivity(mainIntent)
        }
    }
}