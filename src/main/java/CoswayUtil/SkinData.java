package CoswayUtil;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.Bukkit;

import java.util.UUID;

public class SkinData {

    private final String value;
    private final String signature;
    private final UUID uuid;
    private final String name;


    public SkinData(UUID uuid, String name, String value, String signature) {

        this.uuid = uuid;
        this.name = name;
        this.value = value;
        this.signature = signature;
    }


    public String getValue() {
        return value;
    }


    public String getSignature() {
        return signature;
    }


    public UUID getUuid() {
        return uuid;
    }


    public String getName() {
        return name;
    }


    /**
     * Converts Mojang skin data into a Paper player profile.
     */
    public ResolvableProfile getProfile() {

        return ResolvableProfile.resolvableProfile()
                .uuid(uuid)
                .name(name)
                .addProperty(
                        new ProfileProperty(
                                "textures",
                                value,
                                signature
                        )
                )
                .build();
    }
}