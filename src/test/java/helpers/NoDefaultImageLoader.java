package helpers;

public class NoDefaultImageLoader extends ImageLoader{
    public NoDefaultImageLoader(String path) {
        super(path);
    }

    @Override
    String getDefaultPath() {
        return "/images/does_not_exist.png";
    }
}
