package ch.gianlucafrei.nellygateway.cookies;


public class OidcStateCookie {

    public static final String NAME = "oidc-state";

    private String provider;
    private String sate;
    private String nonce;
    private String returnUrl;

    public OidcStateCookie() {}

    public OidcStateCookie(String provider, String sate, String nonce, String returnUrl) {
        this.provider = provider;
        this.sate = sate;
        this.nonce = nonce;
        this.returnUrl =returnUrl;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getSate() {
        return sate;
    }

    public void setSate(String sate) {
        this.sate = sate;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }
}
