package nz.ac.canterbury.seng302.gardenersgrove.config;

import nz.ac.canterbury.seng302.gardenersgrove.entity.PlantInfo;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PlantInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Config class to initialize default plant info data
 * Data belongs to https://perenual.com
 */

@Component
public class PlantInfoDataInitializer implements CommandLineRunner {

    @Autowired
    private PlantInfoRepository plantInfoRepository;

    /**
     * Add Default plants to repo
     * @param args incoming main method arguments
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        if (plantInfoRepository.count() == 0) {
            List<PlantInfo> defaultPlants = Arrays.asList(
                    new PlantInfo(4627L,
                            "Lettuce",
                            "Lactuca Sativa 'Merlot'",
                            "https://perenual.com/storage/species_image/4627_lactuca_sativa_merlot/og/52591617943_f131931cd7_b.jpg",
                            "Lettuce (Lactuca sativa) is a popular, leafy, green vegetable. It's easy to grow and a key ingredient in many salads. Crisp, crunchy varieties offer a refreshing, cool taste, while soft, buttery varieties may have a sweeter flavor. Lettuce come in many shapes, sizes, and colors, from soft green Romaine hearts to wavy, ruffled leaves. It is a great source of essential vitamins and minerals such as iron, calcium, magnesium, potassium, and Vitamins A, C, and K. Nowadays, lettuce can be found in grocery stores year-round and is also popular among home-gardeners, due to its easy-to-grow nature.",
                            "",
                            ""),
                    new PlantInfo(7409L,
                            "Potato",
                            "Solanum Tuberosum",
                            "https://perenual.com/storage/species_image/7409_solanum_tuberosum/og/4999049328_1d9acd50d3_b.jpg",
                            "",
                            "",
                            ""),
                    new PlantInfo(2320L,
                            "Carrot",
                            "Daucus Carota Var. Sativus",
                            "https://perenual.com/storage/species_image/2320_daucus_carota_var_sativus/medium/baby-carrot-healthy-vegetable-roots-nutrition-fresh-food.jpg",
                            "The carrot (Daucus carota var. sativus) is an amazingly versatile vegetable prized for its edible roots. With a sweet flavor, crunchy texture, and a myriad of uses, these delicious veggies are an essential part of many recipes. Rich in carbohydrates and minerals such as calcium, iron and magnesium, carrots are a nutritional powerhouse. They’re also packed with carotenoids, like beta-carotene, lutein, zeaxanthin, and lycopene, essential for eye health and a strong immune system. Plus, they’re low in calories, so they can make a great addition to any diet. Carrots are also easy to grow at home, making them an ideal choice for cooks looking to add more vegetables to their plate.",
                            "",
                            ""),
                    new PlantInfo(5833L,
                            "Parsley",
                            "Petroselinum Crispum",
                            "https://perenual.com/storage/species_image/5833_petroselinum_crispum/og/24890552915_bc32127f01_b.jpg",
                            "Parsley (Petroselinum crispum) is an herb used widely in cooking. It is a biennial plant with curly or flat leaves, occurring naturally in temperate climates. Rich in vitamins and minerals, parsley has many culinary and medicinal uses. Its leaves are often used to add piquancy to dishes, such as salads, soups, sauces and fish. Its roots can be boiled and added to soups. Parsley seed can also be used for flavoring. Parsley can be added fresh (dried is less aromatic) to recipes at the end of cooking for the greatest flavor.",
                            "",
                            ""),
                    new PlantInfo(5842L,
                            "Bean",
                            "Phaseolus Vulgaris",
                            "https://perenual.com/storage/species_image/5842_phaseolus_vulgaris/og/6132751172_d0885c3543_b.jpg",
                            "Bean (Phaseolus vulgaris) is a widely grown annual legume, mainly used for human consumption. It is an herbaceous plant with thin stems, typically reaching up to 40 cm in height. The leaves are simple, alternate and variable in form, from round to oval with pointed tips. The seeds are green or yellowish, oval-shaped with a flattened side and covered in a seed coat. They have a rich, nutty flavor and are often used dried, canned, frozen, or cooked. This species is extremely hearty and easy to cultivate, making it a popular crop with a wide range of culinary uses.",
                            "",
                            ""),
                    new PlantInfo(1888L,
                            "Lemon",
                            "Citrus Limon",
                            "https://perenual.com/storage/species_image/1888_citrus_limon/og/52457383899_cd1df60899_b.jpg",
                            "Lemon (Citrus limon) is an amazing plant species with many culinary and medicinal uses. Its tartness makes it a great companion to savory dishes, and its bright, citrus flavor can elevate sweet treats. Lemon juice is high in potassium and vitamin C, helping to improve immunity, and its acidic properties can work to reduce bacteria in the body and counter inflammation. Plus, lemon zest is a great way to add flavor to dishes without adding extra calories. Lemons are incredibly versatile, and they can even be used topically to brighten skin or hair, making them a great beauty and health product.",
                            "",
                            ""),
                    new PlantInfo(3013L,
                            "Strawberry",
                            "Fragaria 'Allstar'",
                            "https://perenual.com/storage/species_image/3013_fragaria_allstar/medium/52154169748_994ac32109_b.jpg",
                            "The strawberry, Fragaria 'Allstar', is an amazing plant species that produces juicy, flavorful fruit. Its vivid red color adds an elegant beauty to any garden. The 'Allstar' variety is disease-resistant and easy-to-grow, adapting to many climates and soils, making it an ideal pick for gardeners of all types. Each plant grows large, plump berries in abundance that ripen in early summer. Not only are these berries delicious, but they are also a great source of numerous vitamins and minerals, perfect for a healthy snack! With this variety, growing and enjoying your own sweet strawberries has never been easier.",
                            "",
                            ""),
                    new PlantInfo(5194L,
                            "Apple Mint",
                            "Mentha Suaveolens",
                            "https://perenual.com/storage/species_image/5194_mentha_suaveolens/og/2560px-Mentha_suaveolens_kz02.jpg",
                            "Apple mint (Mentha suaveolens) is an aromatic mint with attractive foliage and pleasant fragrance. The leaves are small, oval shaped and fuzzy with serrated edges. Its stem is square, whereas the flower spikes are white or purplish in color. This mint's flavor is similar to spearmint, with a hint of apple-like sweetness. Apple mint is a great addition to several dishes from salads to teas, ice-cream and desserts. Its minty aroma helps to freshen up the air and repel insect pests. Plant it either in the garden or a container for a decorative and functional addition to your home.",
                            "",
                            ""),
                    new PlantInfo(6345L,
                            "Dwarf Plum",
                            "Prunus Domestica 'Johnson' STARKING DELICIOUS",
                            "https://perenual.com/storage/species_image/6345_prunus_domestica_johnson_starking_delicious/og/2560px-Damson_28Prunus_domestica_subsp._insititia29_281988448663129.jpg",
                            "Dwarf Plum is a small cultivar of Prunus domestica. This deciduous tree variety can grow to a maximum height of 10 - 15 feet, with a spread of 8 - 10 feet. Its glossy, dark green foliage is winter hardy and its distinctive white flowers bloom in the spring and set abundant dark purple fruit in the summer. The tree's real attraction is the extremely sweet deep red-purple flesh, giving it its trademark name: Starking Delicious. Dwarf Plum is a great choice for the home gardener, bringing a taste of sweetness to backyard orchards.",
                            "",
                            ""),
                    new PlantInfo(5025L,
                            "Tomato",
                            "Lycopersicon Esculentum 'Rapunzel'",
                            "https://perenual.com/storage/species_image/5025_lycopersicon_esculentum_rapunzel/og/pexels-photo-6314416.jpg",
                            "Tomato (Lycopersicon esculentum 'Rapunzel') is an heirloom variety of tomato, popularly grown in Germany for centuries. Its large fruits are bright yellow, slightly elongated, and with thick walls. The taste is sweet and slightly tart, making it great for salads, canning, and sauces. Its strong vines can reach heights of up to 12 feet, and it is prolific producer with fruits that can weigh up to one pound. It is a very adaptable variety, accepting most soil and climate conditions without problems. It is both early season and indeterminate, so expect to have plenty of tomatoes on hand once they start bearing!",
                            "",
                            ""),
                    new PlantInfo(1848L,
                            "Spider Plant",
                            "Chlorophytum 'Fire Flash'",
                            "https://perenual.com/storage/species_image/1848_chlorophytum_fire_flash/og/2560px-Chlorophytum_orchidastrum_2016-04-28_9101.jpg",
                            "Spider plant (Chlorophytum 'Fire Flash') is an amazing species of plant. Its long, spindly leaves feature beautiful white or yellow variegations that resemble flashes of light. It's a great choice for those who want to brighten up dark corners, as it has a vining habit that can reach up to 3 feet long. Its air-purifying abilities combined with its low maintenance requirements make it an ideal choice for a busy household. Spider plants are also incredibly resilient and can tolerate neglect and poor growing conditions. Plus, its delicate flowers are an irresistible bonus. With its eye-catching foliage and easy-care attitude, Spider Plant 'Fire Flash' is sure to bring a spark to any home.",
                            "",
                            ""),
                    new PlantInfo(5257L,
                            "Swiss Cheese Plant",
                            "Monstera Deliciosa",
                            "https://perenual.com/storage/species_image/5257_monstera_deliciosa/og/4630938853_623dc33137_b.jpg",
                            "The gorgeous Swiss cheese plant (Monstera deliciosa) is a fast-growing, evergreen variety of tropical plant with gorgeous, perforated, glossy foliage. Its striking rhomboid-shaped, deeply-lobed leaves, along with its attractive form and size, make it a popular houseplant. Swiss cheese plants prefer consistently warm temperatures, ample indirect sunlight, and occasional fertilizer. When grown outdoors in warm climates, they produce cone-shaped, yellow-green fruits that are edible and often likened to pineapple. As its common name implies, the older leaves become embossed with deep splits and holes, making it resemble Swiss cheese. Middle-aged plants develop large, ovate, deeply-lobed leaves that are a real showstopper.",
                            "",
                            ""),
                    new PlantInfo(1115L,
                            "Lady Fern",
                            "Athyrium Filix-Femina",
                            "https://perenual.com/storage/species_image/1115_athyrium_filix-femina/og/52345779831_2ee17d3997_b.jpg",
                            "The Lady Fern (Athyrium filix-femina) is simply an amazing plant. With its delicate fronds in a deep green color, this plant is a standout in any garden. Lady Ferns can handle the sun better than most ferns, so these plants can be used in sunny or shaded garden beds. The fronds can be anywhere from 8 inches to 3 feet tall and up to 9 feet wide when mature, making it a great choice for adding texture to landscape beds. Additionally, they are incredibly low maintenance and the perfect plant for those who don't have a lot of time or experience pruning or watering plants. All in all, the Lady Fern is sure to brighten any outdoor space and make a stunning addition to any garden.",
                            "",
                            ""),
                    new PlantInfo(8551L,
                            "Golden Barrel Cactus",
                            "Echinocactus Grusonii",
                            "https://perenual.com/storage/species_image/8551_echinocactus_grusonii/og/52623515667_8855ba17de_b.jpg",
                            "Golden Barrel Cactus (Echinocactus grusonii) is a spiny, globe-shaped cactus native to Mexico. These cacti range in size from a few inches to a few feet in diameter, with mature plants reaching up to 3 feet in height. It has a distinctively golden-yellow color, caused by the thread-like hairs that cover the outside of each spines. The species is extremely drought-tolerant and can thrive in tropical climates. It is also relatively easy to care for—just make sure the soil is constantly dry. Suited for both indoors and outdoor spaces, golden barrel cactus is a delightful addition to any home.",
                            "",
                            ""),
                    new PlantInfo(5808L,
                            "Baby Rubber Plant",
                            "Peperomia Obtusifolia",
                            "https://perenual.com/storage/species_image/5808_peperomia_obtusifolia/og/51191210278_2b58c66af0_b.jpg",
                            "Baby rubber plant (Peperomia obtusifolia) is an evergreen perennial native to tropical regions of the Americas. The plant's thick, glossy, roundish leaves are a light gray green color and have a unique dimpled texture, resembling elephant skin. It is an easy to care for plant that does best in bright, indirect sunlight and requires occasional watering. Baby rubber plant also produces small spikes of white to pale green flowers that are great for adding texture and depth to any room's décor. A great choice for busy gardeners, this variety is both attractive and low-maintenance.",
                            "",
                            ""),
                    new PlantInfo(7684L,
                            "French Marigold",
                            "Tagetes Patula",
                            "https://perenual.com/storage/species_image/7684_tagetes_patula/og/40714507155_c4e49c3955_b.jpg",
                            "The French marigold (Tagetes patula), is a popular garden flower for adding bright and vibrant color. As a tender perennial, it produces a profusion of dainty flower heads in a range of colors including yellow, orange, and mahogany. Growing strong stems, versatile French marigolds can be used in borders, beds, and in containers. A low maintenance variety, French Marigolds have a long blooming season which extends from summer to fall. To keep them looking healthy and vibrant, water regularly and give a trim now and then to encourage more blooms.",
                            "",
                            ""),
                    new PlantInfo(4014L,
                            "Bigleaf Hydrangea",
                            "Hydrangea Macrophylla",
                            "https://perenual.com/storage/species_image/4014_hydrangea_macrophylla/og/52443902558_ff94589d33_b.jpg",
                            "Bigleaf Hydrangea (Hydrangea macrophylla) is a popular deciduous shrub known for its large, showy flower clusters and attractive green leaves. It produces flowers in shades of pink and blue, depending on the soil pH. The multiple flowers open in spring, sometimes lasting up to 8 weeks. Bigleaf Hydrangea prefers moist soils and shade, making it ideal for woodland gardens or as a foundation planting. Drought-tolerant once established, these hydrangeas require little maintenance, with pruning only recommended to control size and shape.",
                            "",
                            ""),
                    new PlantInfo(3088L,
                            "Gardenia",
                            "Gardenia Jasminoides 'Bab1183' SUMMER SNOW",
                            "https://perenual.com/storage/species_image/3088_gardenia_jasminoides_bab1183_summer_snow/og/28553385630_697fc0e971_b.jpg",
                            "Gardenia jasminoides 'Bab1183' SUMMER SNOW is an amazing plant species. It has a classic, captivating fragrance and features fragrant, white, double flowers. It is a truly lovely addition to any garden or landscaping. This species is versatile and can be easily grown in almost any climate, as well as outdoors or in containers. It is also relatively low maintenance, as it only requires weekly watering and reaches a compact size of around 4 feet. These year-round blooms in the spring and summer months give off a delightful scent, making it one of the more popular gardenias around. All in all, this species offers beautiful blooms, resilience, and year-round interest, making it a perfect choice for any garden.",
                            "",
                            ""),
                    new PlantInfo(6803L,
                            "Shrub Rose",
                            "Rosa 'BAIing' GRANDMA'S BLESSING",
                            "https://perenual.com/storage/species_image/6803_rosa_baiing_grandmas_blessing/og/roses_pink_rose_bloom-652962.jpg",
                            "Shrub Rose (Rosa 'BAIing' Grandma's Blessing) is a hardy and drought-resistant rose with vibrant flowers in shades of pink and mauve. Its unique hybridization is bred to provide an abundant seasonal display and tolerance for a wide range of conditions. Its wide arching branches bear large clusters of blossoms up to 1.5 inches across in each flush of bloom. Foliage is attractive and fragrant. Resistant to common pests and diseases, it is easy to care for and maintain. Perfect for garden beds, lawns, or containers, its long-lasting display of blooms will bring your garden to life.",
                            "",
                            ""),
                    new PlantInfo(2044L,
                            "Cabbage Tree",
                            "Cordyline Australis 'Salsa'",
                            "https://perenual.com/storage/species_image/2044_cordyline_australis_salsa/og/Cordyline_australis_27Red_Sensation27_Plant_1900px.jpg",
                            "The cabbage tree (Cordyline australis 'Salsa') is a truly amazing plant species. It features beautiful rich green foliage and a stunning red new growth. Its foliage also has a unique texture, with large, flexible leaves, unlike any other plant. This makes the cabbage tree an eye-catching addition to any garden. It can grow up to 6 meters tall and 3 meters wide, making it an excellent privacy screen. Not only is it visually striking, it also survives well in coastal conditions, making it a versatile and dependable garden plant. With its unique foliage and hardy nature, the cabbage tree is an outstanding addition to any outdoor area.",
                            "",
                            "")
            );
            plantInfoRepository.saveAll(defaultPlants);
        }
    }
}
