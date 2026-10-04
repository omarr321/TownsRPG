package helpers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ResourcesTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "/images/debugging/DebugImage.png",
            "/images/debugging/LightTest.png",
            "/images/debugging/UVGrid.png"
    })
    void requiredDebugImageExists(String path) {
        assertNotNull(getClass().getResource(path), "Missing image: " + path);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/images/objects/Anvil.png",
            "/images/objects/Armchair.png",
            "/images/objects/Bookshelf.png",
            "/images/objects/BookStack.png",
            "/images/objects/Bucket.png",
            "/images/objects/Cabinet.png",
            "/images/objects/Candle.png",
            "/images/objects/Chair.png",
            "/images/objects/Chest.png",
            "/images/objects/Crate.png",
            "/images/objects/Desk.png",
            "/images/objects/Door.png",
            "/images/objects/Fridge.png",
            "/images/objects/Globe.png",
            "/images/objects/Mirror.png",
            "/images/objects/Painting.png",
            "/images/objects/Potions.png",
            "/images/objects/PottedPlant.png",
            "/images/objects/Radio.png",
            "/images/objects/Safe.png",
            "/images/objects/Shield.png",
            "/images/objects/Skull.png",
            "/images/objects/Sofa.png",
            "/images/objects/Table.png",
            "/images/objects/Torch.png",
            "/images/objects/Vase.png",
            "/images/objects/WallClock.png",
            "/images/objects/Window.png",
    })
    void requiredObjImageExists(String path) {
        assertNotNull(getClass().getResource(path), "Missing image: " + path);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/images/floors/CarpetFloor.png",
            "/images/floors/CheckerFloor.png",
            "/images/floors/ConcreteFloor.png",
            "/images/floors/DarkWoodFloor.png",
            "/images/floors/FlagstoneFloor.png",
            "/images/floors/MarbleFloor.png",
            "/images/floors/TerracottaFloor.png",
            "/images/floors/WoodFloor.png",

    })
    void requiredFloorImageExists(String path) {
        assertNotNull(getClass().getResource(path), "Missing image: " + path);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/images/walls/BrickWall.png",
            "/images/walls/ConcreteWall.png",
            "/images/walls/PlasterWall.png",
            "/images/walls/StoneWall.png",
            "/images/walls/StripedWallpaper.png",
            "/images/walls/WainscotWall.png",
            "/images/walls/WoodPanelWall.png",
    })
    void requiredWallImageExists(String path) {
        assertNotNull(getClass().getResource(path), "Missing image: " + path);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/images/ceilings/BeamCeiling.png",
            "/images/ceilings/CofferedCeiling.png",
            "/images/ceilings/DropTileCeiling.png",
            "/images/ceilings/IndustrialCeiling.png",
            "/images/ceilings/PlasterCeiling.png",
            "/images/ceilings/WoodPlankCeiling.png",
    })
    void requiredCeilingImageExists(String path) {
        assertNotNull(getClass().getResource(path), "Missing image: " + path);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/branding/colors.txt",
            "/branding/icon.svg",
            "/branding/logo.svg"
    })
    void requiredBrandingExists(String path) {
        assertNotNull(getClass().getResource(path), "Missing branding: " + path);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/images/UI/arrow_left.png",
            "/images/UI/arrow_right.png",
            "/images/UI/arrow_straight.png",
            "/images/UI/arrow_left_x.png",
            "/images/UI/arrow_right_x.png",
            "/images/UI/arrow_straight_x.png",
            "/images/UI/cursors/cursor.png",
            "/images/UI/cursors/cursor-clicked.png",
            "/images/UI/cursors/cursor-highlighted.png"
    })
    void requiredUIExists(String path) {
        assertNotNull(getClass().getResource(path), "Missing UI: " + path);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/fonts/Tuffy.ttf",
            "/fonts/Tuffy_Bold.ttf",
            "/fonts/untyped.ttf"
    })
    void requiredFontExists(String path) {
        assertNotNull(getClass().getResource(path), "Missing font: " + path);
    }
}
