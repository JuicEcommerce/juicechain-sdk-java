import org.juicechain.JuicEchain;
import org.juicechain.exceptions.IssueException;
import org.juicechain.exceptions.NotAuthorizedException;
import org.juicechain.exceptions.TransferException;
import org.juicechain.managed.Asset;
import org.juicechain.managed.Node;
import org.juicechain.models.AssetType;
import org.juicechain.models.Balance;
import org.juicechain.managed.Wallet;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Random;

public class BasicTest {

    private Node demo;
    private Wallet wallet;
    private Wallet wallet2;
    private String signature;
    private Asset asset;
    private String assetName;

    @Test
    public void connect_to_node(){
        demo = JuicEchain.getNode("demo", "", "");
        Assert.assertNotNull(this.demo);
    }

    @Test(dependsOnMethods={"connect_to_node"})
    public void create_wallet() throws NotAuthorizedException, IOException {
        wallet = demo.createWallet();

        Assert.assertNotNull(this.wallet);
        Assert.assertEquals("demo", wallet.node);
    }

    // Create signature (required for next calls)
    @Test(dependsOnMethods={"create_wallet"})
    public void generate_signature(){
        signature = wallet.getAuthorization("");

        Assert.assertNotNull(signature);
    }

    // Issue new Asset
    @Test(dependsOnMethods={"generate_signature"})
    public void create_asset() throws NotAuthorizedException, IOException, IssueException, ParseException {
        assetName = getRandomAssetName();
        asset = demo.issue(assetName, "Mein Test Asset", AssetType.admission,
                100  , wallet.address, "BackToTheFuture GmbH");

        Assert.assertNotNull(asset);
        Assert.assertEquals(asset.issuer, wallet.address);
    }

    /*
    // Verify Balance of issuer
    @Test(dependsOnMethods={"create_wallet"})
    public void fetch_balance() throws NotAuthorizedException, IOException, InterruptedException {
        Thread.sleep(2000);
        List<Balance> balance = wallet.getBalance( 0);

        Assert.assertNotNull(balance);
        Assert.assertEquals(balance.size(), 1);
        Assert.assertEquals(balance.get(0).asset, asset.name);
    }*/


    // Attach card image (for mobile wallet) to the asset
    @Test(dependsOnMethods={"create_asset"})
    public void set_mobile_card() throws NotAuthorizedException, IOException {
        boolean result = asset.setCard(assetName, "test/examples/card.png");

        Assert.assertEquals(result, true);
    }

    // Attach media (image) (for mobile wallet) to the asset
    @Test(dependsOnMethods={"create_asset"})
    public void set_mobile_media() throws NotAuthorizedException, IOException {
        boolean result = asset.setMedia(assetName, "test/examples/media.png");

        Assert.assertEquals(result, true);
    }

    // Transfer asset to target wallet
    @Test(dependsOnMethods={"create_asset"})
    public void transfer() throws NotAuthorizedException, IOException, TransferException {
        // create target wallet
        wallet2 = demo.createWallet();

        // transfer
        boolean successTransfer = wallet.transfer(wallet2.address, assetName, 2, "{}");
        Assert.assertEquals(true, successTransfer);

        // check balance
        List<Balance> balance = wallet2.getBalance();
        Assert.assertNotNull(balance);
        Assert.assertEquals(balance.size(), 1);
        Assert.assertEquals(balance.get(0).asset, asset.name);
        Assert.assertEquals(balance.get(0).quantity, 2);
    }

    /**
     * Generate random string for free asset name
     *
     * @return
     */
    private static String getRandomAssetName(){
        Random rand = new Random();
        int n = rand.nextInt(50000);
        return "demo:testasset:" + n;
    }

}