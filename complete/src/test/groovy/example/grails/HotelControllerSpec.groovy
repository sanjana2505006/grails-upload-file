package example.grails

import grails.testing.web.controllers.ControllerUnitTest
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.mock.web.MockMultipartFile
import spock.lang.Specification

class HotelControllerSpec extends Specification implements ControllerUnitTest<HotelController> {

    void setup() {
        controller.uploadHotelFeaturedImageService = Mock(UploadHotelFeaturedImageService)
        controller.crudMessageService = Mock(CrudMessageService)
    }

    void "an upload that loses a race with another update shows the form again with a message"() {
        given:
        request.method = 'POST'
        request.addFile(new MockMultipartFile('featuredImageFile', 'photo.png', 'image/png', [1] as byte[]))
        params.id = 5
        params.version = 0

        when:
        controller.uploadFeaturedImage()

        then:
        1 * controller.uploadHotelFeaturedImageService.uploadFeaturedImage(_) >> {
            throw new OptimisticLockingFailureException('row was updated by another transaction')
        }
        flash.message == 'Another user has updated this Hotel while you were editing'
        response.redirectedUrl == '/hotel/editFeaturedImage/5'
    }
}
