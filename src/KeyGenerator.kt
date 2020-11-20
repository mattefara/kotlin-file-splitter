import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class KeyGenerator {

    companion object {
        val ITERRATION_COUNT = 512
        val KEY_LENGTH = 256
        val ALGORITHM = "PBKDF2WithHmacSHA256"

        @JvmStatic
        fun generateKey(password: String, salt: ByteArray): SecretKey {
            val factory = SecretKeyFactory.getInstance(ALGORITHM)
            val spec = PBEKeySpec(password.toCharArray(), salt, ITERRATION_COUNT, KEY_LENGTH)
            return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
        }

        fun generateSalt(length: Int = 16) : ByteArray {
            return SecureRandom().generateSeed(length)
        }

    }

}

fun Cipher.generateDecryptCipher(password: String, iv : ByteArray, salt: ByteArray) {
    val key = KeyGenerator.generateKey(password, salt)
    this.apply {
        init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
    }
}

fun Cipher.generateCryptCipher(password: String, salt: ByteArray = KeyGenerator.generateSalt()) {
    val key = KeyGenerator.generateKey(password, salt)
    this.apply {
        init(Cipher.ENCRYPT_MODE, key)
    }
}