package example.grails

import grails.testing.mixin.integration.Integration
import org.apache.grails.testing.http.client.HttpClientSupport
import spock.lang.Specification
import spock.lang.Unroll

@Integration
class PagesSpec extends Specification implements HttpClientSupport {

    RestaurantDataService restaurantDataService
    PointOfInterestDataService pointOfInterestDataService
    HotelDataService hotelDataService

    @Unroll
    void "the #controller pages render"() {
        given:
        Long id = save(controller)

        expect:
        http("/${controller}/index").assertStatus(200).assertContains('Example')
        http("/${controller}/create").assertStatus(200).assertContains('name="name"')
        http("/${controller}/show/${id}").assertStatus(200).assertContains('Example')
        http("/${controller}/edit/${id}").assertStatus(200).assertContains('name="version"')
        http("/${controller}/editFeaturedImage/${id}").assertStatus(200)
                .assertContains('enctype="multipart/form-data"')
                .assertContains('name="featuredImageFile"')

        cleanup:
        delete(controller, id)

        where:
        controller << ['restaurant', 'pointOfInterest', 'hotel']
    }

    @Unroll
    void "a #controller name edit from an outdated form is rejected"() {
        given:
        Long id = save(controller)
        Map<String, Object> form = [_method: 'PUT', id: id, version: 0]
        httpPostForm("/${controller}/update", form + [name: 'First']).assertStatus(200)

        when: 'a second form, opened before the first edit, is submitted'
        def response = httpPostForm("/${controller}/update", form + [name: 'Second'])

        then:
        response.assertStatus(200)
        response.assertContains("Another user has updated this ${label} while you were editing")
        http("/${controller}/show/${id}").assertContains('First')

        cleanup:
        delete(controller, id)

        where:
        controller        | label
        'restaurant'      | 'Restaurant'
        'pointOfInterest' | 'Point of Interest'
        'hotel'           | 'Hotel'
    }

    private Long save(String controller) {
        switch (controller) {
            case 'restaurant' -> restaurantDataService.save('Example').id
            case 'pointOfInterest' -> pointOfInterestDataService.save('Example').id
            default -> hotelDataService.save('Example').id
        }
    }

    private void delete(String controller, Long id) {
        switch (controller) {
            case 'restaurant' -> restaurantDataService.delete(id)
            case 'pointOfInterest' -> pointOfInterestDataService.delete(id)
            default -> hotelDataService.delete(id)
        }
    }
}
