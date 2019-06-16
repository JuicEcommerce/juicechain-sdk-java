package org.juicechain.models;

import java.util.Date;

/**
 * Chain Native Asset Parameters
 *
 */
public class AssetParams {

    // From this date the asset is "valid"
    public Date inception;

    // Until this date the asset is "valid"
    public Date experiation;

    // Asset is invalid
    public boolean valid;

    // Asset was disabled and is not available
    public boolean disabled;

}
