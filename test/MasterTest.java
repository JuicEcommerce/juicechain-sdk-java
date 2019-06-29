import org.juicechain.JuicEchain;
import org.juicechain.exceptions.IssueException;
import org.juicechain.exceptions.NotAuthorizedException;
import org.juicechain.exceptions.NotFoundException;
import org.juicechain.managed.Asset;
import org.juicechain.models.AssetType;
import org.juicechain.managed.Node;
import org.juicechain.models.AssetParams;
import org.juicechain.models.Balance;
import org.juicechain.managed.Wallet;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;

public class MasterTest {

    private Node demo;
    private Wallet wallet;
    private Asset masterAsset;
    private String authentication;
    private Wallet wallet2;
    private String masterName;

    @Test
    public void connect_to_node(){
        demo = JuicEchain.getNode("demo", "", "");
        Assert.assertNotNull(this.demo);
    }

    @Test(dependsOnMethods={"connect_to_node"})
    public void create_wallet() throws NotAuthorizedException, IOException {
        wallet = demo.createWallet();
        wallet2 = demo.createWallet();

        Assert.assertNotNull(wallet);
        Assert.assertNotNull(wallet2);
        Assert.assertEquals("demo", wallet.node);
    }

    // Create authentication (required for next calls)
    @Test(dependsOnMethods={"create_wallet"})
    public void generate_authentication(){
        authentication = wallet.getAuthentication("");

        Assert.assertNotNull(authentication);
    }

    // Create Master asset (with media)
    @Test(dependsOnMethods={"generate_authentication"})
    public void create_master_asset() throws NotAuthorizedException, IOException, IssueException, ParseException {
        masterName = getRandomAssetName() + "#";

        masterAsset = demo.issue(masterName, "Mein Master Asset", AssetType.admission,
                1,wallet.address, "BackToTheFuture GmbH");

        Assert.assertNotNull(masterAsset);
        Assert.assertEquals(masterAsset.master, true);
    }

    // Issue NFT
    @Test(dependsOnMethods={"create_master_asset"})
    public void create_none_fungible() throws ParseException, NotAuthorizedException, IOException, IssueException {
        AssetParams params = new AssetParams();
        params.inception = (new SimpleDateFormat("yyyy-MM-dd")).parse("2018-05-05");
        params.experiation = (new SimpleDateFormat("yyyy-MM-dd")).parse("2018-06-05");

        Asset nft = demo.issueNFT(masterName + "1", wallet2.address, "", params, 1, authentication);
        Assert.assertEquals(nft.name, masterName + "1");
    }

    @Test(dependsOnMethods={"create_none_fungible"})
    public void verify_none_fungible() throws NotAuthorizedException, IOException, NotFoundException, ParseException {
        List<Balance> balance = wallet2.getBalance( 0);

        Assert.assertNotNull(balance);
        Assert.assertEquals(balance.size(), 1);

        Asset nft = wallet2.getAsset(masterName + "1");

        Assert.assertEquals(nft.inception, (new SimpleDateFormat("yyyy-MM-dd")).parse("2018-05-05"));
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
